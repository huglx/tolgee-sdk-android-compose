package cz.fit.cvut.feature.translation.domain.usecases

import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.translation.presentation.common.viewmodel.SingleTranslationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

internal class ObserveTranslationsUseCase(
    private val translateRepository: TranslationsRepository,
    private val langRepository: LanguageRepository,
) {
    operator fun invoke(keyName: String): Flow<SingleTranslationState> {
        return combine(
            langRepository.selectedLanguage,
            translateRepository.observeTranslationForKey(keyName)
        ) { selectedLang, translation ->
            when {
                translation == null -> {
                    // Create a TolgeeKeyModel with empty translations but with key name and available languages
                    val languages = langRepository.getLanguages()

                    return@combine createEmptyTranslation(
                        keyName = keyName,
                        languages = languages,
                        selectedLang = selectedLang
                    )
                }

                else -> SingleTranslationState.Available(
                    translation = translation,
                    selectedLanguage = selectedLang
                )
            }
        }.distinctUntilChanged()
    }

    private fun createEmptyTranslation(
        keyName: String,
        languages: List<TolgeeLanguageModel>,
        selectedLang: String
    ): SingleTranslationState.Available {
        val emptyTranslation = TolgeeKeyModel(
            keyName = keyName,
            translations = languages.associate { it.tag to TolgeeTranslationModel(0L, "", it) },
            keyId = 0L
        )
        
        return SingleTranslationState.Available(
            translation = emptyTranslation,
            selectedLanguage = selectedLang
        )
    }
}