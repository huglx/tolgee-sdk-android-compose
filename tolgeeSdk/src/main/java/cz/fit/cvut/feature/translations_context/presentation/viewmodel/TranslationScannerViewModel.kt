package cz.fit.cvut.feature.translations_context.presentation.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.core.common.utils.Constants.END_MARKER
import cz.fit.cvut.core.common.utils.Constants.START_MARKER
import cz.fit.cvut.core.common.utils.decodeText
import cz.fit.cvut.feature.translations_context.domain.TolgeeKeyMeta
import cz.fit.cvut.feature.translations_context.domain.repository.KeyMetaRepository
import cz.fit.cvut.feature.translations_context.utils.RouteManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import radiography.ExperimentalRadiographyComposeApi
import radiography.ScanScopes
import radiography.ScannableView
import java.util.Stack
import kotlin.math.max

/**
 * ViewModel responsible for managing the translation scanning process
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class TranslationScannerViewModel(
    private val repository: KeyMetaRepository
) : ViewModel() {
    private var lastScanTime = 0L
    private val MIN_SCAN_INTERVAL = 1000L
    private val PERIODIC_SCAN_INTERVAL = 3000L
    private val MAX_HIERARCHY_DEPTH = 50 // Maximum depth for hierarchy traversal

    // Status of scanning process
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _currentScreenId = MutableStateFlow<String?>(null)
    val currentScreenId: StateFlow<String?> = _currentScreenId
    
    // Tracks if periodic scanning is active
    private val _isPeriodicScanningActive = MutableStateFlow(false)
    val isPeriodicScanningActive = _isPeriodicScanningActive.asStateFlow()
    
    // Background scanning job
    private var periodicScanJob: Job? = null
    
    // States for overlay UI
    sealed class OverlayState {
        data object Loading : OverlayState()
        data class Success(val positions: List<TolgeeKeyMeta>) : OverlayState()
        data object Empty : OverlayState()
        
        override fun toString(): String {
            return when (this) {
                is Loading -> "Loading"
                is Success -> "Success(${positions.size} positions)"
                is Empty -> "Empty"
            }
        }
    }
    
    // Overlay state that automatically updates from repository data
    private val _overlayState = MutableStateFlow<OverlayState>(OverlayState.Loading)
    val overlayState: StateFlow<OverlayState> = _overlayState

    init {
        // Setup reactive flow to automatically update UI when data changes
        viewModelScope.launch {
            _currentScreenId
                .filterNotNull()
                .flatMapLatest { screenId ->
                    repository.getKeysFlowByScreen(screenId)
                        .onEach { positions ->
                            // Log only when positions change significantly
                            Log.d("TranslationScannerViewModel", "Received positions update: ${positions.size} positions")
                        }
                        .map { positions ->
                            if (_isScanning.value) {
                                OverlayState.Loading
                            } else if (positions.isEmpty()) {
                                OverlayState.Empty
                            } else {
                                OverlayState.Success(positions)
                            }
                        }
                        .catch { error ->
                            Log.e("TranslationScannerViewModel", "Error in flow: ${error.message}", error)
                            emit(OverlayState.Empty)
                        }
                }
                .collect { state ->
                    _overlayState.value = state
                }
        }
    }
    
    /**
     * Starts periodic scanning for translations
     * @param isContentRendered Flag indicating if the content is rendered and ready for scanning
     */
    fun startPeriodicScanning(isContentRendered: Boolean) {
        if (periodicScanJob != null) return
        
        _isPeriodicScanningActive.value = true
        
        // Initial scan
        if (isContentRendered) {
            val currentRoute = RouteManager.getCurrentRoute()
            scanForTranslations(currentRoute)
        }
        
        // Start periodic scanning
        periodicScanJob = viewModelScope.launch {
            while (_isPeriodicScanningActive.value) {
                delay(PERIODIC_SCAN_INTERVAL)
                
                if (isContentRendered && shouldScanNow()) {
                    val currentRoute = RouteManager.getCurrentRoute()
                    // Use suspend version to avoid nested launches
                    scanForTranslationsSuspend(currentRoute)
                }
            }
        }
    }
    
    /**
     * Stops periodic scanning
     */
    fun stopPeriodicScanning() {
        viewModelScope.launch {
            _isPeriodicScanningActive.value = false
            periodicScanJob?.cancelAndJoin()
            periodicScanJob = null
        }
    }
    
    override fun onCleared() {
        stopPeriodicScanning()
        super.onCleared()
    }

    /**
     * Initiates a scan of the UI hierarchy to find translations.
     * Can be called directly from a coroutine scope.
     * @param screenId Screen identifier for scanning, can be null
     * @return true if scan was performed, false if it was skipped
     */
    private fun scanForTranslations(screenId: String?): Boolean {
        // Input validation
        val safeScreenId = screenId?.trim() ?: ""
        if (safeScreenId.length > 100) {
            Log.w("TranslationScannerViewModel", "Screen ID too long, truncating")
        }
        val truncatedScreenId = safeScreenId.take(100)
        
        // Skip if already scanning or if scanned too recently
        if (_isScanning.value) {
            return false
        }
        
        if (!shouldScanNow()) {
            return false
        }

        // Update current screen ID and mark scan time
        _currentScreenId.value = truncatedScreenId
        lastScanTime = System.currentTimeMillis()
        
        // Launch new scan in viewModelScope
        viewModelScope.launch {
            performScan(truncatedScreenId)
        }
        
        return true
    }
    
    /**
     * Performs the actual scan operation. This is an internal method that should be called 
     * from within a coroutine scope.
     */
    private fun performScan(screenId: String) {
        _isScanning.value = true

        try {
            repository.clearScreenTranslations(screenId)
            
            scanUIHierarchy(screenId)
            
            val results = repository.getKeysByScreen(screenId)
            Log.d("TranslationScannerViewModel", "Scan completed, found ${results.size} positions")

        } catch (e: Exception) {
            Log.e("TranslationScannerViewModel", "Error scanning UI: ${e.message}", e)
        } finally {
            _isScanning.value = false
        }
    }
    
    /**
     * Initiates a scan synchronously from within a coroutine.
     * Use this when already in a coroutine context to avoid nested launches.
     */
    private fun scanForTranslationsSuspend(screenId: String?) {
        // Input validation
        val safeScreenId = screenId?.trim() ?: ""
        if (safeScreenId.length > 100) {
            Log.w("TranslationScannerViewModel", "Screen ID too long, truncating")
        }
        val truncatedScreenId = safeScreenId.take(100)
        
        // Skip if already scanning or if scanned too recently
        if (_isScanning.value) {
            return
        }
        
        if (!shouldScanNow()) {
            return
        }

        // Update current screen ID and mark scan time
        _currentScreenId.value = truncatedScreenId
        lastScanTime = System.currentTimeMillis()
        
        // Directly perform scan without launching a new coroutine
        performScan(truncatedScreenId)
    }

    /**
     * Checks if enough time has passed since the last scan
     */
    private fun shouldScanNow(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastScanTime > MIN_SCAN_INTERVAL)
    }

    /**
     * Performs the actual UI hierarchy scan
     * @param screenId Screen identifier
     */
    @OptIn(ExperimentalRadiographyComposeApi::class)
    private fun scanUIHierarchy(screenId: String) {
        try {
            // Get all roots (windows) in the current process
            val roots = ScanScopes.AllWindowsScope.findRoots()

            var elementsFound = 0
            
            // Scan each root view with depth limitation
            roots.forEach { root ->
                traverseHierarchyIterative(root, MAX_HIERARCHY_DEPTH) { view ->
                    when (view) {
                        is ScannableView.ComposeView -> {
                            if (processComposeView(view, repository, screenId)) {
                                elementsFound++
                            }
                        }
                        is ScannableView.ChildRenderingError -> {
                            Log.e("TranslationScannerViewModel", "Error rendering child: $view")
                        }
                        else -> { /* Ignore other view types */ }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TranslationScannerViewModel", "Error scanning UI hierarchy: ${e.message}", e)
            // Don't propagate the exception to avoid interrupting app execution
        }
    }

    /**
     * Process a Compose view to extract and register translation information
     * @return true if a translation was found and registered
     */
    @OptIn(ExperimentalRadiographyComposeApi::class)
    private fun processComposeView(
        view: ScannableView.ComposeView?,
        repository: KeyMetaRepository,
        screenId: String
    ): Boolean {
        // Check for null
        if (view == null) return false
        
        try {
            val isInvisible = view.semanticsConfigurations.any {
                it.contains(SemanticsProperties.InvisibleToUser)
            }
            
            if (isInvisible) {
                return false // Skip invisible elements
            }
            
            val text = view.semanticsConfigurations
                .firstOrNull { it.contains(SemanticsProperties.Text) }
                ?.get(SemanticsProperties.Text)
                ?.toString()
                ?: return false

            // Check for markers and valid text
            if (!text.contains(START_MARKER) && !text.contains(END_MARKER)) {
                return false
            }

            val semanticsNode = view.semanticsNodes.firstOrNull() ?: return false
            val bounds = semanticsNode.boundsInWindow
            
            // Validate bounds dimensions
            if (bounds == null || !isValidRect(bounds)) {
                return false
            }

            val keyName = decodeText(text)
                ?.takeIf { it.isNotBlank() }
                ?: return false

            // Register position in repository
            repository.registerKeyPosition(
                keyName = keyName,
                rect = bounds,
                screenName = screenId
            )
            
            return true
        } catch (e: Exception) {
            Log.e("TranslationScannerViewModel", "Error processing Compose view: ${e.message}")
            return false
        }
    }
    
    private fun isValidRect(rect: Rect): Boolean {
        return rect.width > 0 && rect.height > 0 && 
               !rect.width.isNaN() && !rect.height.isNaN() &&
               !rect.left.isNaN() && !rect.top.isNaN()
    }

    /**
     * Traverse the view hierarchy iteratively to avoid stack overflows
     * with heavily nested compositions
     */
    @OptIn(ExperimentalRadiographyComposeApi::class)
    private fun traverseHierarchyIterative(
        root: ScannableView, 
        maxDepth: Int,
        onVisitNode: (ScannableView) -> Unit
    ) {
        val stack = Stack<Pair<ScannableView, Int>>()
        stack.push(root to 0)
        
        while (stack.isNotEmpty()) {
            val (node, depth) = stack.pop()
            
            // Process current node
            onVisitNode(node)
            
            // Stop if we've reached max depth
            if (depth >= maxDepth) continue
            
            // Add children to stack
            try {
                // Convert to list to safely work with children
                val childrenList = node.children.toList()
                // Add in reverse order for depth-first traversal
                childrenList.reversed().forEach { child ->
                    stack.push(child to depth + 1)
                }
            } catch (e: Exception) {
                Log.e("TranslationScannerViewModel", "Error processing children: ${e.message}")
            }
        }
    }
} 