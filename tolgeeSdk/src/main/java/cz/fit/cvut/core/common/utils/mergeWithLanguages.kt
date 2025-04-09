package cz.fit.cvut.core.common.utils

import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel

internal fun List<TolgeeKeyModel>.mapWithLanguages(languages: List<TolgeeLanguageModel>): List<TolgeeKeyModel> {
    val languagesMap = languages.associateBy { it.tag }

    return this.map { keyModel ->
        keyModel.copy(
            translations = keyModel.translations.mapValues { (lang, translation) ->
                translation.copy(language = languagesMap[lang])
            }
        )
    }
}
