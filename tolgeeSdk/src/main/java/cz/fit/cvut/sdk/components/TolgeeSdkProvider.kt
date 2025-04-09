package cz.fit.cvut.sdk.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import cz.fit.cvut.feature.init.presentation.viewmodel.TolgeeInitViewModel
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageViewModel
import cz.fit.cvut.feature.translations_context.presentation.overlay.TranslationOverlay
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import cz.fit.cvut.sdk.core.TolgeeSdk
import cz.fit.cvut.sdk.core.TolgeeSdkInitializer
import kotlinx.coroutines.delay

/**
 * Access to the current TolgeeSdk instance
 */
object LocalTolgeeSdk {
    val current: TolgeeSdk
        @Composable
        @ReadOnlyComposable
        get() = LocalTolgeeSdkState.current
}

internal val LocalTolgeeSdkState = staticCompositionLocalOf<TolgeeSdk> {
    error("No TolgeeSDK provided")
}

/**
 * Access to the initialization view model
 */
internal object LocalInitViewModel {
    val current: TolgeeInitViewModel
        @Composable
        @ReadOnlyComposable
        get() = LocalInitViewModelState.current
}

private val LocalInitViewModelState = staticCompositionLocalOf<TolgeeInitViewModel> {
    error("No InitViewModel provided")
}

/**
 * Access to the language view model
 */
internal object LocalLanguageViewModel {
    val current: TolgeeLanguageViewModel
        @Composable
        @ReadOnlyComposable
        get() = LocalLanguageViewModelState.current
}

private val LocalLanguageViewModelState = staticCompositionLocalOf<TolgeeLanguageViewModel> {
    error("No LanguageViewModel provided")
}

/**
 * Main provider for Tolgee SDK functionality.
 * Wraps content and provides debugging overlay when in edit mode.
 * 
 * @param content The content to be wrapped with SDK functionality
 */
@Composable
fun TolgeeProvider(content: @Composable () -> Unit) {
    // Get the SDK instance from the initializer
    val sdk = TolgeeSdkInitializer.getInstance()
    
    // Get viewModels from the SDK
    val initViewModel: TolgeeInitViewModel = remember { sdk.getKoin().get() }
    val languageViewModel: TolgeeLanguageViewModel = remember { sdk.getKoin().get() }
    
    // State to track content rendering
    var isContentRendered by remember { mutableStateOf(false) }
    var contentPositionedCount by remember { mutableIntStateOf(0) }
    var parentBounds by remember { mutableStateOf(Rect.Zero) }

    CompositionLocalProvider(
        LocalTolgeeSdkState provides sdk,
        LocalInitViewModelState provides initViewModel,
        LocalLanguageViewModelState provides languageViewModel,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    // Update parent bounds
                    parentBounds = coordinates.boundsInWindow()
                    contentPositionedCount++
                }
            
        ) {
            content()

            // Show debug button in DEBUG mode, but disabled until content is rendered
            if (sdk.mode == TolgeeSdkMode.DEBUG) {
                DraggableDebugButton(
                    onClick = { 
                        // Only toggle editing if content is rendered
                        if (isContentRendered) {
                            sdk.toggleEditing() 
                        }
                    },
                    isEnabled = isContentRendered
                )
            }

            // Check if content is rendered before showing overlay
            LaunchedEffect(contentPositionedCount) {
                // Use a sequential count to determine if content layout is stable
                // If no changes in a short time, we consider content rendered
                val currentCount = contentPositionedCount
                delay(300)
                
                // If the count didn't change during the delay, consider content rendered
                if (currentCount == contentPositionedCount && contentPositionedCount > 0) {
                    isContentRendered = true
                }
            }

            TranslationOverlay(parentBounds, isContentRendered)
        }
    }
}

/**
 * For backward compatibility, we keep this overload that accepts an explicit SDK instance
 */
@Composable
fun TolgeeProvider(sdk: TolgeeSdk, content: @Composable () -> Unit) {
    val initViewModel: TolgeeInitViewModel = remember { sdk.getKoin().get() }
    val languageViewModel: TolgeeLanguageViewModel = remember { sdk.getKoin().get() }
    
    // State to track content rendering
    var isContentRendered by remember { mutableStateOf(false) }
    var contentPositionedCount by remember { mutableIntStateOf(0) }
    var parentBounds by remember { mutableStateOf(Rect.Zero) }

    CompositionLocalProvider(
        LocalTolgeeSdkState provides sdk,
        LocalInitViewModelState provides initViewModel,
        LocalLanguageViewModelState provides languageViewModel,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    // Update parent bounds
                    parentBounds = coordinates.boundsInWindow()
                    contentPositionedCount++
                }
            
        ) {
            content()

            // Show debug button in DEBUG mode, but disabled until content is rendered
            if (sdk.mode == TolgeeSdkMode.DEBUG) {
                DraggableDebugButton(
                    onClick = { 
                        // Only toggle editing if content is rendered
                        if (isContentRendered) {
                            sdk.toggleEditing() 
                        }
                    },
                    isEnabled = isContentRendered
                )
            }

            // Check if content is rendered before showing overlay
            LaunchedEffect(contentPositionedCount) {
                // Use a sequential count to determine if content layout is stable
                // If no changes in a short time, we consider content rendered
                val currentCount = contentPositionedCount
                delay(300)
                
                // If the count didn't change during the delay, consider content rendered
                if (currentCount == contentPositionedCount && contentPositionedCount > 0) {
                    isContentRendered = true
                }
            }

            TranslationOverlay(parentBounds, isContentRendered)
        }
    }
}

