package cz.fit.cvut.feature.translations_context.data

import androidx.compose.ui.geometry.Rect
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translations_context.domain.TolgeeKeyMeta
import cz.fit.cvut.feature.translations_context.domain.repository.KeyMetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filter
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Implementation of KeyMetaRepository that manages translation key positions
 * @param scope CoroutineScope used for emitting flow updates
 */
internal class KeyMetaRepositoryImpl(
    private val scope: CoroutineScope
): KeyMetaRepository {
    
    // Using thread-safe collection for storing keys
    private val keysList = CopyOnWriteArrayList<TolgeeKeyMeta>()

    // Using SharedFlow for publishing updates with buffer
    private val _keysMetaFlow = MutableSharedFlow<List<TolgeeKeyMeta>>(
        replay = 1,
        extraBufferCapacity = 10
    )
    
    private val keysMetaFlow: SharedFlow<List<TolgeeKeyMeta>> = _keysMetaFlow.asSharedFlow()

    init {
        // Emit initial empty list
        scope.launch {
            _keysMetaFlow.emit(emptyList())
        }
    }

    override fun registerKeyPosition(keyId: Long, rect: Rect, screenName: String) {
        // Input validation
        if (keyId <= 0 || screenName.isBlank() || !isValidRect(rect)) {
            Log.w("KeyMetaRepository", "Invalid parameters for keyId: $keyId")
            return
        }

        Log.d("KeyMetaRepository", "Registering position for keyId: $keyId on screen: $screenName")
        
        // Find existing element
        val existingIndex = keysList.indexOfFirst { it.keyId == keyId }
        
        if (existingIndex >= 0) {
            // Update existing element
            val existing = keysList[existingIndex]
            val updated = TolgeeKeyMeta(
                keyId = keyId,
                keyName = existing.keyName,
                position = rect,
                screenId = screenName,
                namespace = existing.namespace
            )
            keysList[existingIndex] = updated
        } else {
            // Add new element
            keysList.add(TolgeeKeyMeta(
                keyId = keyId,
                keyName = null,
                position = rect,
                screenId = screenName,
                namespace = null
            ))
        }
        
        // Emit update to the flow
        scope.launch {
            _keysMetaFlow.emit(keysList.toList())
        }
    }

    override fun registerKeyPosition(keyName: String, rect: Rect, screenName: String) {
        // Input validation
        if (keyName.isBlank() || screenName.isBlank() || !isValidRect(rect)) {
            Log.w("KeyMetaRepository", "Invalid parameters for keyName: $keyName")
            return
        }

        Log.d("KeyMetaRepository", "Registering position for keyName: $keyName on screen: $screenName")
        
        // Find existing element
        val existingIndex = keysList.indexOfFirst { it.keyName == keyName }
        
        if (existingIndex >= 0) {
            // Update existing element
            val existing = keysList[existingIndex]
            val updated = TolgeeKeyMeta(
                keyName = keyName,
                keyId = existing.keyId,
                namespace = existing.namespace,
                position = rect,
                screenId = screenName
            )
            keysList[existingIndex] = updated
        } else {
            // Add new element
            keysList.add(TolgeeKeyMeta(
                keyName = keyName,
                keyId = null,
                namespace = null,
                position = rect,
                screenId = screenName
            ))
        }
        
        // Emit update to the flow
        scope.launch {
            _keysMetaFlow.emit(keysList.toList())
        }
    }

    /**
     * Validates rectangle dimensions
     */
    private fun isValidRect(rect: Rect): Boolean {
        return rect.width > 0 && 
               rect.height > 0 && 
               !rect.width.isNaN() && 
               !rect.height.isNaN() &&
               rect.width < 10000 && // Reasonable size limits
               rect.height < 10000 && 
               !rect.left.isNaN() && 
               !rect.top.isNaN()
    }

    override fun findKeyPosition(keyId: Long): TolgeeKeyMeta? {
        // CopyOnWriteArrayList is thread-safe, no synchronization needed
        return keysList.find { it.keyId == keyId }
    }

    override fun findKeyPosition(keyName: String): TolgeeKeyMeta? {
        // CopyOnWriteArrayList is thread-safe, no synchronization needed
        return keysList.find { it.keyName == keyName }
    }

    override fun getKeysByScreen(screenName: String): List<TolgeeKeyMeta> {
        // CopyOnWriteArrayList is thread-safe, no synchronization needed
        val result = keysList.filter { it.screenId == screenName }
        Log.d("KeyMetaRepository", "getKeysByScreen($screenName) returning ${result.size} items")
        return result
    }
    
    override fun observeKeysByScreen(screenName: String): Flow<List<TolgeeKeyMeta>> {
        return keysMetaFlow.map { list -> 
            val filtered = list.filter { it.screenId == screenName }
            Log.d("KeyMetaRepository", "observeKeysByScreen flow for $screenName: ${filtered.size} items")
            filtered
        }
    }

    override fun clearAllPositions() {
        Log.d("KeyMetaRepository", "Clearing all positions")
        // Clear the list and emit empty list
        keysList.clear()
        scope.launch {
            _keysMetaFlow.emit(emptyList())
        }
    }

    override fun clearScreenTranslations(screenName: String) {
        if (screenName.isBlank()) {
            Log.w("KeyMetaRepository", "Cannot clear translations for blank screen name")
            return
        }
        
        Log.d("KeyMetaRepository", "Clearing translations for screen: $screenName")
        
        // Create new list without elements of the specified screen
        val elementsToKeep = keysList.filter { it.screenId != screenName }
        
        // Clear existing list and add filtered elements
        keysList.clear()
        keysList.addAll(elementsToKeep)
        
        // Emit updated list
        scope.launch {
            _keysMetaFlow.emit(keysList.toList())
        }
    }

    override fun getKeysFlowByScreen(screenId: String): Flow<List<TolgeeKeyMeta>> {
        Log.d("KeyMetaRepository", "Creating flow for screen: $screenId, total keys: ${keysList.size}")
        return keysMetaFlow.map { keys ->
            val filtered = keys.filter { it.screenId == screenId }
            Log.d("KeyMetaRepository", "Filtered keys for screen $screenId: ${filtered.size}")
            filtered
        }
    }
}