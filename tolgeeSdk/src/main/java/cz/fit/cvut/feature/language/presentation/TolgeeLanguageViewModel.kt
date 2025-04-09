package cz.fit.cvut.feature.language.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.core.common.utils.InitStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class TolgeeLanguageViewModel(
    private val languageRepository: LanguageRepository,
    private val initStateManager: InitStateManager
) : ViewModel() {

    private val _languages = MutableStateFlow<LanguageState>(LanguageState.IsLoading)
    val languages: StateFlow<LanguageState> = _languages.asStateFlow()

    val selectedLanguage = languageRepository.selectedLanguage

    init {
        loadLanguages()
    }

    /**
     * Set the selected language and store it in preferences
     * Launch this in a coroutine to avoid blocking
     */
    fun setSelectedLanguage(language: String) {
        viewModelScope.launch {
            languageRepository.setSelectedLanguage(language)
        }
    }

    fun loadLanguages() {
        viewModelScope.launch {
            initStateManager.waitForInitialization()

            _languages.value = LanguageState.IsLoading
            
            when (val result = languageRepository.findLanguages()) {
                is ResultWrapper.Success -> {
                    _languages.value = LanguageState.Loaded(result.data)
                }
                is ResultWrapper.Error -> {
                    _languages.value = LanguageState.Error(result.message)
                }
            }
        }
    }
}

internal sealed interface LanguageState {
    data class Loaded(val languages: List<TolgeeLanguageModel>): LanguageState
    data object IsLoading: LanguageState
    data class Error(val message: String): LanguageState
} 