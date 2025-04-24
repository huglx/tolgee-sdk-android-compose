package cz.fit.cvut.demo.screens.translations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageDropdown
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.feature.translations_context.utils.LocalScreenId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationsScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Translate(
                        keyName = "translations_appbar_title",
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    TolgeeLanguageDropdown()
                }
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        startY = 0f,
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Translation Examples",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Simple translations
                TranslationSection(
                    title = "Simple Translations",
                    items = listOf(
                        "common.welcome" to "Welcome message",
                        "common.hello" to "Hello greeting",
                        "common.goodbye" to "Goodbye message"
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App UI translations
                TranslationSection(
                    title = "UI Elements",
                    items = listOf(
                        "button.save" to "Save button",
                        "button.cancel" to "Cancel button",
                        "dialog.confirmation" to "Confirmation dialog"
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error messages
                TranslationSection(
                    title = "Error Messages",
                    items = listOf(
                        "error.network" to "Network error",
                        "error.unauthorized" to "Authorization error",
                        "error.not_found" to "Not found error"
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Parameterized translations
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Parameterized Translations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        TranslationWithParams(
                            key = "message.greeting",
                            description = "Greeting with name parameter",
                            params = mapOf("name" to "John")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TranslationWithParams(
                            key = "message.items",
                            description = "Items count message",
                            params = mapOf("count" to "5")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TranslationWithParams(
                            key = "message.price",
                            description = "Price formatting",
                            params = mapOf("price" to "99.99", "currency" to "USD")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Translate(keyName = "button.back")
                }
            }
        }
    }
}

@Composable
fun TranslationSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            items.forEach { (key, description) ->
                TranslationItem(key = key, description = description)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TranslationItem(
    key: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(12.dp)
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Translate(
            keyName = key,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TranslationWithParams(
    key: String,
    description: String,
    params: Map<String, String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(12.dp)
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Parameters: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            
            Text(
                text = params.entries.joinToString { "${it.key}=${it.value}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        
        Translate(
            keyName = key,
            modifier = Modifier.padding(top = 4.dp),
            params = params
        )
    }
} 