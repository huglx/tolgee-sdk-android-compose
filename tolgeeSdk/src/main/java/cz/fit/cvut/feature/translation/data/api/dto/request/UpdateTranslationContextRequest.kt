package cz.fit.cvut.feature.translation.data.api.dto.request

internal data class UpdateTranslationContextRequest(
    var name: String,
    var relatedKeysInOrder: List<RelatedKeys>,
    var translations: Map<String, String>,
)

internal data class RelatedKeys(
    var keyName: String,
    var namespace: String? = null
)
