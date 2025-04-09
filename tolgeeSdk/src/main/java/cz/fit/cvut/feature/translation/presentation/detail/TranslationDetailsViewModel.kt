package cz.fit.cvut.feature.translation.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.domain.usecases.UpdateTranslationNoContextUseCase
import cz.fit.cvut.feature.translation.domain.usecases.UpdateTranslationWithContextUseCase
import cz.fit.cvut.core.common.utils.handleApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class TranslationDetailsViewModel(
    private val updateNoContextUseCase: UpdateTranslationNoContextUseCase,
    private val updateWithContextUseCase: UpdateTranslationWithContextUseCase,
    private val translationsRepository: TranslationsRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    fun updateTranslation(keyName: String, newTranslations: Map<String, String>) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading

            handleApiResponse(
                apiCall = { updateNoContextUseCase(keyName, newTranslations) },
                onSuccess = { _ ->
                    _updateState.value = UpdateState.Success
                },
                onError = { error ->
                    _updateState.value = UpdateState.Error(error)
                }
            )
        }
    }

    fun updateTranslationWithContext(
        keyName: String,
        newTranslations: Map<String, String>,
        keyId: Long
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading

            handleApiResponse(
                apiCall = { updateWithContextUseCase(keyName, keyId, newTranslations) },
                onSuccess = { _ ->
                    _updateState.value = UpdateState.Success
                },
                onError = { error ->
                    _updateState.value = UpdateState.Error(error)
                }
            )
        }
    }
}

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Loading : UpdateState
    data object Success : UpdateState
    data class Error(val message: String) : UpdateState
}
