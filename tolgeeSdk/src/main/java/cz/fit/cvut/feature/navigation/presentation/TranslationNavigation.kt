package cz.fit.cvut.feature.navigation.presentation

import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel

internal sealed class TranslationNavigation {
    data class SingleTranslation(
        val keyModel: TolgeeKeyModel
    ) : TranslationNavigation()
    
    data object TranslationsList : TranslationNavigation()
    
    data object None : TranslationNavigation()

    data class Error(
        val message: String,
    ) : TranslationNavigation()
} 