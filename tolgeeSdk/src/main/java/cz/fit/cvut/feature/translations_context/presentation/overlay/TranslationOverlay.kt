package cz.fit.cvut.feature.translations_context.presentation.overlay

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigation
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigationViewModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.presentation.detail.TranslationDetailsScreen
import cz.fit.cvut.feature.translation.presentation.list.TranslationListScreen
import cz.fit.cvut.feature.translations_context.domain.TolgeeKeyMeta
import cz.fit.cvut.feature.translations_context.presentation.viewmodel.TranslationScannerViewModel
import cz.fit.cvut.feature.translations_context.presentation.viewmodel.TranslationScannerViewModel.OverlayState
import cz.fit.cvut.sdk.components.LocalTolgeeSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Composable that draws individual borders for each translation
 * without blocking interaction with UI elements underneath
 */

@Composable
internal fun TranslationOverlay(parentBounds: Rect, isContentRendered: Boolean) {
    val sdk = LocalTolgeeSdk.current
    val navigationViewModel: TranslationNavigationViewModel = remember { sdk.getKoin().get() }
    val isEditing by sdk.isEditing.collectAsState()
    val scope = rememberCoroutineScope()
    val scannerViewModel: TranslationScannerViewModel = remember { sdk.getKoin().get() }
    val navigationState by navigationViewModel.navigation.collectAsState()

    // Manage scanning based on editing mode
    ManageScanningEffect(isEditing, isContentRendered, scannerViewModel)

    if (!isEditing) return

    // Display overlay based on state
    DisplayOverlay(scannerViewModel, parentBounds, navigationViewModel)

    // Add TranslationDialog
    TranslationDialog(
        navigationState = navigationState,
        navigationViewModel = navigationViewModel,
        scope = scope
    )
}

@Composable
private fun ManageScanningEffect(
    isEditing: Boolean, 
    isContentRendered: Boolean, 
    scannerViewModel: TranslationScannerViewModel
) {
    // Effect to start/stop periodic scanning when editing mode changes
    DisposableEffect(isEditing) {
        if (isEditing) {
            scannerViewModel.startPeriodicScanning(isContentRendered)
        } else {
            scannerViewModel.stopPeriodicScanning()
        }
        
        onDispose {
            // Make sure scanning stops on dispose
            scannerViewModel.stopPeriodicScanning()
        }
    }
}

@Composable
private fun DisplayOverlay(
    scannerViewModel: TranslationScannerViewModel,
    parentBounds: Rect,
    navigationViewModel: TranslationNavigationViewModel
) {
    val state by scannerViewModel.overlayState.collectAsState()
    
    // Debug logging
    LaunchedEffect(state) {
        Log.d("TranslationOverlay", "Current overlay state: $state")
    }

    when (state) {
        is OverlayState.Loading -> {
            ShowLoadingIndicator()
        }

        is OverlayState.Success -> {
            val positions = (state as OverlayState.Success).positions
            ShowTranslationBoxes(
                positions = positions,
                parentBounds = parentBounds,
                onLongClick = { keyName ->
                    navigationViewModel.navigateToTranslationDetails(keyName)
                }
            )
        }

        is OverlayState.Empty -> {
            // Nothing to show
        }
    }
}

@Composable
private fun ShowLoadingIndicator() {
    // Cache modifiers
    val boxModifier = remember { Modifier.fillMaxSize() }
    val indicatorModifier = remember { Modifier.size(48.dp) }
    
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = indicatorModifier,
            color = Color.Red,
            strokeWidth = 4.dp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShowTranslationBoxes(
    positions: List<TolgeeKeyMeta>,
    parentBounds: Rect,
    onLongClick: (String) -> Unit
) {
    val density = LocalDensity.current
    
    // Cache common modifiers and values
    val rootModifier = remember { Modifier.fillMaxSize() }
    val borderColor = remember { Color.Red }
    val borderWidth = remember { 2.dp }
    
    Log.d("ShowTranslationBoxes", "Rendering ${positions.size} boxes")
    
    Box(modifier = rootModifier) {
        // Use stable keys and minimize position-dependent recalculations
        positions.forEach { pos ->
            // Only draw valid boxes
            if (pos.position.width > 0 && pos.position.height > 0 && pos.keyName != null) {
                val x = with(density) { (pos.position.left - parentBounds.left).toDp() }
                val y = with(density) { (pos.position.top - parentBounds.top).toDp() }
                val width = with(density) { pos.position.width.toDp() }
                val height = with(density) { pos.position.height.toDp() }
                
                val posKey = remember(pos.keyName) {
                    pos.keyName.hashCode() +
                    pos.position.left.hashCode() + 
                    pos.position.top.hashCode() + 
                    pos.position.width.hashCode() + 
                    pos.position.height.hashCode()
                }
                
                key(posKey) {
                    // Cache modifier for this specific box
                    val boxModifier = remember {
                        Modifier
                            .absoluteOffset(x = x, y = y)
                            .size(width, height)
                            .border(borderWidth, borderColor)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { 
                                    onLongClick(pos.keyName) 
                                }
                            )
                    }

                    Box(modifier = boxModifier)
                }
            }
        }
    }
}

@Composable
private fun TranslationDialog(
    navigationState: TranslationNavigation,
    navigationViewModel: TranslationNavigationViewModel,
    scope: CoroutineScope
) {
    when (navigationState) {
        is TranslationNavigation.SingleTranslation -> {
            TranslationDetailsDialog(
                keyModel = navigationState.keyModel,
                onDismiss = { scope.launch { navigationViewModel.closeNavigation() } },
                onClose = { scope.launch { navigationViewModel.navigateToTranslationsList() } }
            )
        }
        is TranslationNavigation.TranslationsList -> {
            TranslationListDialog(
                onDismiss = { scope.launch { navigationViewModel.closeNavigation() } },
                onClose = { scope.launch { navigationViewModel.closeNavigation() } },
                onTranslationClick = { scope.launch { navigationViewModel.navigateToTranslationDetails(it) } }
            )
        }
        else -> {}
    }
}

@Composable
private fun TranslationDetailsDialog(
    keyModel: TolgeeKeyModel,
    onDismiss: () -> Unit,
    onClose: () -> Unit
) {
    // Cache common modifier
    val surfaceModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(modifier = surfaceModifier) {
            TranslationDetailsScreen(
                key = keyModel,
                onClose = onClose
            )
        }
    }
}

@Composable
private fun TranslationListDialog(
    onDismiss: () -> Unit,
    onClose: () -> Unit,
    onTranslationClick: (TolgeeKeyModel) -> Unit
) {
    // Cache common modifier
    val surfaceModifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(modifier = surfaceModifier) {
            TranslationListScreen(
                onClose = onClose,
                onTranslationClick = onTranslationClick
            )
        }
    }
}