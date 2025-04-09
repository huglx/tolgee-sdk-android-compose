package cz.fit.cvut.feature.translation.domain.models

import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel

internal data class TolgeeTranslationModel (
    var id: Long?,
    var text: String?,
    var language: TolgeeLanguageModel?,
)