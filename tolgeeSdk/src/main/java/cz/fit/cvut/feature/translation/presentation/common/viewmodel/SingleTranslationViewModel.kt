package cz.fit.cvut.feature.translation.presentation.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.usecases.ObserveTranslationsUseCase
import cz.fit.cvut.core.common.utils.InitStateManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class SingleTranslationViewModel(
    private val keyName: String,
    private val observeTranslationsUseCase: ObserveTranslationsUseCase,
    private val initStateManager: InitStateManager
) : ViewModel() {
    private val _state = MutableStateFlow<SingleTranslationState>(SingleTranslationState.IsLoading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            initStateManager.waitForInitialization()

            observeTranslationsUseCase(keyName)
                .collect { state ->
                    _state.value = state
                }
        }
    }
}

internal sealed interface SingleTranslationState {
    data class Available(
        val translation: TolgeeKeyModel,
        val selectedLanguage: String
    ) : SingleTranslationState

    data object NotFound : SingleTranslationState

    data object IsLoading: SingleTranslationState

    data class Error(val message: String): SingleTranslationState
} 