package cz.fit.cvut.feature.translations_context.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal to provide the screen identifier across the composition tree
 */
val LocalScreenId = staticCompositionLocalOf<String?> { null }

/**
 * Remembers the current screen context from LocalScreenId
 */
@Composable
internal fun rememberScreenContext(): String? {
    val screenId = LocalScreenId.current

    return remember(screenId) {
        screenId
    }
}