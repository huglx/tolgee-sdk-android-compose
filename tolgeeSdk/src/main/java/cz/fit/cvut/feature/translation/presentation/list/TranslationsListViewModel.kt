package cz.fit.cvut.feature.translation.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.usecases.GetTranslationsForAvailableLanguagesUseCase
import cz.fit.cvut.core.common.utils.handleApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class TranslationsListViewModel(
    private val translationsRepository: TranslationsRepository,
    private val getTranslationsForAvailableLanguagesUseCase: GetTranslationsForAvailableLanguagesUseCase
): ViewModel() {
    private val _state = MutableStateFlow<TranslationListState>(TranslationListState.IsLoading)
    val state get() = _state.asStateFlow()

    init {
        loadTranslations()
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            handleApiResponse(
                apiCall = { getTranslationsForAvailableLanguagesUseCase() },
                onSuccess = { translations ->
                    _state.value = TranslationListState.Loaded(translations)
                },
                onError = { error ->
                    _state.value = TranslationListState.Error(error)
                }
            )
        }
    }


    sealed interface TranslationListState {
        data class Loaded(val translations: List<TolgeeKeyModel>): TranslationListState
        data object IsLoading: TranslationListState
        data class Error(val message: String): TranslationListState
    }
}