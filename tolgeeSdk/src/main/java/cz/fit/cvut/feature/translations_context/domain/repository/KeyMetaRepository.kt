package cz.fit.cvut.feature.translations_context.domain.repository

import androidx.compose.ui.geometry.Rect
import cz.fit.cvut.feature.translations_context.domain.TolgeeKeyMeta
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing key positions
 */
internal interface KeyMetaRepository {
    /**
     * Registers a key position
     */
    fun registerKeyPosition(keyId: Long, rect: Rect, screenName: String)

    /**
     * Registers a key position with a key name
     */
    fun registerKeyPosition(keyName: String, rect: Rect, screenName: String)
    /**
     * Gets position data for a key
     */
    fun findKeyPosition(keyId: Long): TolgeeKeyMeta?

    fun findKeyPosition(keyName: String): TolgeeKeyMeta?

    /**
     * Gets all keys for a specific screen
     */
    fun getKeysByScreen(screenName: String): List<TolgeeKeyMeta>
    
    /**
     * Observes all keys for a specific screen with Flow
     */
    fun observeKeysByScreen(screenName: String): Flow<List<TolgeeKeyMeta>>
    
    /**
     * Clears all key positions
     */
    fun clearAllPositions()

    /**
     * Clears all key positions for a specific screen
     */
    fun clearScreenTranslations(screenName: String)

    fun getKeysFlowByScreen(screenId: String): Flow<List<TolgeeKeyMeta>>
} 