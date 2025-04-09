package cz.fit.cvut.feature.translations_context.domain.usecase

import cz.fit.cvut.feature.translations_context.domain.repository.KeyMetaRepository
import androidx.compose.ui.geometry.Rect
import kotlin.math.sqrt

/**
 * Use case for finding neighboring keys
 */
internal class FindNeighborKeysUseCase(
    private val repository: KeyMetaRepository
) {
    /**
     * Finds neighboring translation keys based on their UI position proximity
     * 
     * @param keyName The key name to find neighbors for
     * @param proximityThreshold Maximum distance threshold for neighbors (in pixels)
     * @param maxNeighbors Maximum number of neighbors to return
     * @param sameScreenOnly Whether to only consider keys from the same screen
     * @return List of key names that are considered neighbors, sorted by proximity
     */
    operator fun invoke(
        keyName: String,
        proximityThreshold: Float = Float.POSITIVE_INFINITY,
        maxNeighbors: Int = 5,
        sameScreenOnly: Boolean = true
    ): List<String> {
        val keyMeta = repository.findKeyPosition(keyName) ?: return emptyList()
        val allKeyMetas = repository.getKeysByScreen(keyMeta.screenId ?: "")

        val keyCenter = keyMeta.position.center
        
        return allKeyMetas
            .asSequence()
            .filter { meta ->
                // Filter by screen ID if required
                (!sameScreenOnly || meta.screenId == keyMeta.screenId) &&
                        // Exclude the original key
                        meta.keyName != keyName &&
                        meta.keyName != null
            }
            .map { meta ->
                // Calculate the distance between the centers of the bounding boxes
                val metaCenter = meta.position.center
                val dx = metaCenter.x - keyCenter.x
                val dy = metaCenter.y - keyCenter.y
                val distance = sqrt(dx * dx + dy * dy)
                Pair(meta.keyName!!, distance) // Safe to use !! because we filtered nulls
            }
            .filter { (_, distance) ->
                // Only include keys within the proximity threshold
                distance <= proximityThreshold
            }
            .sortedBy { (_, distance) ->
                // Sort by distance (closest first)
                distance
            }
            .take(maxNeighbors) // Limit to maximum number of neighbors
            .map { (keyName, _) -> keyName }
            .toList()
    }
} 