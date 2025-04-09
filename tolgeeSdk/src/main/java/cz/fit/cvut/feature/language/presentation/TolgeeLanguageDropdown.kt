package cz.fit.cvut.feature.language.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cz.fit.cvut.sdk.components.LocalLanguageViewModel

@Composable
fun TolgeeLanguageDropdown() {
    val viewModel = LocalLanguageViewModel.current
    val languagesState by viewModel.languages.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState("")
    var expanded by remember { mutableStateOf(false) }
    
    // Get language name from tag
    val selectedLanguageName = remember(languagesState, selectedLanguage) {
        when (languagesState) {
            is LanguageState.Loaded -> {
                val languages = (languagesState as LanguageState.Loaded).languages
                languages.find { it.tag == selectedLanguage }?.name ?: selectedLanguage
            }
            else -> selectedLanguage
        }
    }

    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) {
            Text(text = "Language: $selectedLanguageName")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            when (languagesState) {
                is LanguageState.Loaded -> {
                    val languages = (languagesState as LanguageState.Loaded).languages
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(text = "${language.flagEmoji} ${language.name}") },
                            onClick = {
                                viewModel.setSelectedLanguage(language.tag)
                                expanded = false
                            }
                        )
                    }
                }
                is LanguageState.IsLoading -> {
                    DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                }
                is LanguageState.Error -> {
                    DropdownMenuItem(
                        text = { Text("Error loading languages") },
                        onClick = { viewModel.loadLanguages() }
                    )
                }
            }
        }
    }
}
