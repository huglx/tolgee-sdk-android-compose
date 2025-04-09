package cz.fit.cvut.feature.translation.domain.usecases

import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.core.common.utils.mapWithLanguages
import cz.fit.cvut.feature.language.data.LanguageRepository

internal class GetTranslationsForAvailableLanguagesUseCase(
    private val translationsRepository: TranslationsRepository,
    private val langRepository: LanguageRepository
) {
    suspend operator fun invoke(): ResultWrapper<List<TolgeeKeyModel>> {
        return when (val languagesResult = langRepository.findLanguages()) {
            is ResultWrapper.Error -> languagesResult
            is ResultWrapper.Success -> {
                when (val translationsResult = translationsRepository.getTranslations(languagesResult.data)) {
                    is ResultWrapper.Error -> translationsResult
                    is ResultWrapper.Success -> {
                        ResultWrapper.Success(translationsResult.data.mapWithLanguages(languagesResult.data))
                    }
                }
            }
        }
    }
}


