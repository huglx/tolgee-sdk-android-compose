package cz.fit.cvut.feature.translation.domain.models

internal data class TolgeeKeyModel (
    var keyId: Long,
    var keyName: String,
    var translations: Map<String, TolgeeTranslationModel>,
)