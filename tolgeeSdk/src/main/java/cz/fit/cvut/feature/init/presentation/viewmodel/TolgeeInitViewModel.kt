package cz.fit.cvut.feature.init.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.fit.cvut.feature.init.domain.InitRepository
import cz.fit.cvut.core.common.utils.handleApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class TolgeeInitViewModel(
    private val repository: InitRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InitState>(InitState.Loading)
    val state = _state.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = InitState.Loading
            handleApiResponse(
                apiCall = { repository.initFetching() },
                onSuccess = { _ ->
                    _state.value = InitState.Success
                },
                onError = { error ->
                    _state.value = InitState.Error(error)
                }
            )
        }
    }
}

internal sealed interface InitState {
    data object Loading : InitState
    data object Success : InitState
    data class Error(val message: String) : InitState
} 