package cz.fit.cvut.feature.translation.presentation.detail

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.sdk.components.LocalTolgeeSdk

@Composable
internal fun TranslationDetailsScreen(
    key: TolgeeKeyModel,
    onClose: () -> Unit,
) {
    val translations = remember { mutableStateMapOf<String, String>() }
    val context = LocalTolgeeSdk.current
    val viewModel: TranslationDetailsViewModel = remember { context.getKoin().get() }
    val updateState by viewModel.updateState.collectAsState()
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        key.translations.forEach { (lang, translationModel) ->
            translations[lang] = translationModel.text ?: ""
        }
    }

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
                text = "Edit: ${key.keyName}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onClose,
                enabled = updateState !is UpdateState.Loading
            ) {
                Text("Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            key.translations.forEach { (lang, translationModel) ->
                OutlinedTextField(
                    value = translations[lang] ?: "",
                    onValueChange = { translations[lang] = it },
                    label = { Text("${translationModel.language?.name} ${translationModel.language?.flagEmoji}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    Log.i("TranslationDetailsScreen", "saving $translations")
                    viewModel.updateTranslationWithContext(key.keyName, translations, key.keyId)
                },
                enabled = updateState !is UpdateState.Loading
            ) {
                if (updateState is UpdateState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text("Save")
                }
            }
        }

        when (updateState) {
            is UpdateState.Success -> {
                LaunchedEffect(Unit) {
                    onClose()
                    //viewModel.notifyTranslationUpdated()
                }
            }
            is UpdateState.Error -> {
                LaunchedEffect(Unit) {
                    showError = true
                    errorMessage = (updateState as UpdateState.Error).message
                }
            }
            else -> {}
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showError = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
