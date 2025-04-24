package cz.fit.cvut.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.feature.translations_context.utils.LocalScreenId

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit
) {
    CompositionLocalProvider(LocalScreenId provides "login") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Translate(
                keyName = "login.welcome",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Translate(
                keyName = "login.description",
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(onClick = onNavigateToHome) {
                Translate(keyName = "login.button.continue")
            }
        }
    }
} 