package cz.fit.cvut.feature.translation.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.sdk.components.LocalTolgeeSdk

@Composable
internal fun TranslationListScreen(
    onClose: () -> Unit,
    onTranslationClick: (TolgeeKeyModel) -> Unit
) {
    val context = LocalTolgeeSdk.current
    val viewModel: TranslationsListViewModel = remember { context.getKoin().get() }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Translations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onClose) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val result = state) {
            is TranslationsListViewModel.TranslationListState.Loaded -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(result.translations) { translation ->
                        TranslationItem(translation, onTranslationClick)
                    }
                }
            }
            is TranslationsListViewModel.TranslationListState.IsLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                )
            }
            is TranslationsListViewModel.TranslationListState.Error -> {
                Text(
                    text = "Error: ${result.message}",
                    color = Color.Red,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun TranslationItem(
    translation: TolgeeKeyModel,
    onTranslationClick: (TolgeeKeyModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTranslationClick(translation) }
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = translation.keyName,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        translation.translations.forEach { (lang, translationModel) ->
            Text(
                text = "${translationModel.language?.name} ${translationModel.language?.flagEmoji}: ${translationModel.text ?: "No translation"}",
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
