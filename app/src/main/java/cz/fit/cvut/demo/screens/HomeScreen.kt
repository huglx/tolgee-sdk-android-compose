package cz.fit.cvut.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageDropdown
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.feature.translations_context.utils.LocalScreenId

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit
) {
    CompositionLocalProvider(LocalScreenId provides "home") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Translate(
                keyName = "home.welcome",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Translate(
                keyName = "home.welcome",
            ) {
                Text(it.text, color = Color.Gray)
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }



            Translate(
                keyName = "home.description",
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(onClick = onNavigateToLogin) {
                Translate(keyName = "home.button.logout")
            }

            TolgeeLanguageDropdown()
        }
    }
} 