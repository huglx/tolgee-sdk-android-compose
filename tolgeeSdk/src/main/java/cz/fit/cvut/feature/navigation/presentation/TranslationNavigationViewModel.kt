package cz.fit.cvut.feature.navigation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.fit.cvut.core.common.utils.handleApiResponse
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class TranslationNavigationViewModel(
    private val translationsRepository: TranslationsRepository
) : ViewModel() {
    private val _navigation = MutableStateFlow<TranslationNavigation>(TranslationNavigation.None)
    val navigation = _navigation.asStateFlow()
    
    fun navigateToTranslationDetails(key: TolgeeKeyModel) {
        _navigation.value = TranslationNavigation.SingleTranslation(key)
    }

    fun navigateToTranslationDetails(keyId: Long) {
        viewModelScope.launch {
            handleApiResponse(
                apiCall = {
                    translationsRepository.getKeyById(keyId)
                },
                onSuccess = { key ->
                    _navigation.value = TranslationNavigation.SingleTranslation(key)
                },
                onError = { error ->
                    _navigation.value = TranslationNavigation.Error(error)
                }
            )
        }
    }

    fun navigateToTranslationDetails(keyName: String) {
        viewModelScope.launch {
            handleApiResponse(
                apiCall = {
                    translationsRepository.getKeyByName(keyName)
                },
                onSuccess = { key ->
                    _navigation.value = TranslationNavigation.SingleTranslation(key)
                },
                onError = { error ->
                    _navigation.value = TranslationNavigation.Error(error)
                }
            )
        }
    }

    fun navigateToTranslationsList() {
        _navigation.value = TranslationNavigation.TranslationsList
    }
    
    fun closeNavigation() {
        _navigation.value = TranslationNavigation.None
    }
} 