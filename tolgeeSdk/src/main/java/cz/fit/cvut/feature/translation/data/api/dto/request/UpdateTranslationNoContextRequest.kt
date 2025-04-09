package cz.fit.cvut.feature.translation.data.api.dto.request

internal data class UpdateTranslationNoContextRequest(
    val translations: Map<String, String>,
    val key: String
)