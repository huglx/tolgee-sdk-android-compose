package cz.fit.cvut.feature.translation.presentation.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.fit.cvut.core.common.utils.encodeText
import cz.fit.cvut.core.common.utils.formattedText
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigation
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigationViewModel
import cz.fit.cvut.feature.translation.presentation.common.viewmodel.SingleTranslationState
import cz.fit.cvut.feature.translation.presentation.common.viewmodel.SingleTranslationViewModel
import cz.fit.cvut.feature.translation.presentation.detail.TranslationDetailsScreen
import cz.fit.cvut.feature.translation.presentation.list.TranslationListScreen
import cz.fit.cvut.sdk.components.LocalTolgeeSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

@Composable
fun t(
    keyName: String,
    params: Map<String, Any>? = null
): AnnotatedString {
    val sdk = LocalTolgeeSdk.current
    val viewModel: SingleTranslationViewModel = remember { sdk.getKoin().get { parametersOf(keyName) } }
    val state by viewModel.state.collectAsState()
    
    return when (val currentState = state) {
        is SingleTranslationState.Available -> {
            val translationText = remember(currentState.translation, currentState.selectedLanguage) {
                if(currentState.translation.translations[currentState.selectedLanguage]?.text.isNullOrEmpty()) {
                    currentState.translation.keyName
                } else {
                    currentState.translation.translations[currentState.selectedLanguage]?.text!!
                }
            }
            val formattedText = formattedText(translationText, params)
            val encodedId = encodeText(currentState.translation.keyName)
            formattedText + AnnotatedString(encodedId)
        }
        is SingleTranslationState.Error -> throw IllegalStateException("Translation error: ${currentState.message}")
        is SingleTranslationState.IsLoading -> {
            AnnotatedString(keyName)
        }
        else -> throw IllegalStateException("Translation state is not available")
    }
}

@Composable
fun Translate(
    modifier: Modifier = Modifier,
    keyName: String,
    params: Map<String, Any>? = null,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    content: @Composable (translationText: AnnotatedString) -> Unit = { translationText ->
        Text(
            text = translationText,
            style = style,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
        )
    }
) {
    val sdk = LocalTolgeeSdk.current
    val viewModel: SingleTranslationViewModel = remember { sdk.getKoin().get { parametersOf(keyName) } }
    val state by viewModel.state.collectAsState()
    when (val currentState = state) {
        is SingleTranslationState.Available -> {
            val translationText = remember(currentState.translation, currentState.selectedLanguage) {
                if(currentState.translation.translations[currentState.selectedLanguage]?.text.isNullOrEmpty()) {
                    currentState.translation.keyName
                } else {
                    currentState.translation.translations[currentState.selectedLanguage]?.text!!
                }
            }
            val formattedText = formattedText(translationText, params)
            val encodedId = encodeText(currentState.translation.keyName)
            
            content(formattedText + AnnotatedString(encodedId))
        }
        is SingleTranslationState.Error -> ErrorContent(currentState.message)
        is SingleTranslationState.IsLoading -> content(AnnotatedString(keyName))
        else -> {}
    }
}

@Composable
private fun ErrorContent(message: String) {
    Text(text = message, color = Color.Red)
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
}

@Composable
private fun TranslationContent(
    modifier: Modifier,
    translation: TolgeeKeyModel,
    params: Map<String, Any>?,
    selectedLang: String,
    content: @Composable (translationText: AnnotatedString) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        val translationText = remember(translation, selectedLang) {
            if(translation.translations[selectedLang]?.text.isNullOrEmpty()) {
                translation.keyName
            } else {
                translation.translations[selectedLang]?.text!!
            }
        }
        val formattedText = formattedText(translationText, params)
        val encodedId = encodeText(translation.keyName)

        content(formattedText + AnnotatedString(encodedId))
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TranslationListScreen(
                onClose = onClose,
                onTranslationClick = onTranslationClick
            )
        }
    }
}
