package cz.fit.cvut.feature.translations_context.presentation.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import cz.fit.cvut.core.common.utils.decodeId
import cz.fit.cvut.feature.translations_context.utils.rememberScreenContext

/**
 * Extension function for Modifier to track the position of a UI element
 * containing a Tolgee translation key
 */
@Composable
internal fun Modifier.trackTranslationPosition(keyName: String, screenId: String? = null): Modifier {
    val actualScreenId = screenId ?: rememberScreenContext() ?: ""
    
    // Extract key ID from the key name if possible
    val keyId = decodeId(keyName) ?: 0L

    return this.onGloballyPositioned { coordinates ->
        if (keyId > 0) {
            val position = coordinates.boundsInWindow()
            //viewModel.registerElementPosition(keyId, position, actualScreenId)
        }
    }
}