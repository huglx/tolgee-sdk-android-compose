package cz.fit.cvut.core.common.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

internal sealed class InitState {
    object NotInitialized : InitState()
    object Initialized : InitState()
    data class Error(val error: Throwable) : InitState()
}

internal class InitStateManager {
    private val _initializationState = MutableStateFlow<InitState>(InitState.NotInitialized)
    val initializationState: StateFlow<InitState> = _initializationState.asStateFlow()

    suspend fun waitForInitialization() {
        initializationState.first { it is InitState.Initialized }
    }

    fun setInitialized() {
        _initializationState.value = InitState.Initialized
    }

    fun setError(error: Throwable) {
        _initializationState.value = InitState.Error(error)
    }
} 