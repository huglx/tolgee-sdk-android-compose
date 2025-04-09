package cz.fit.cvut.feature.language.domain.models

internal data class TolgeeLanguageModel(
    val id: Long,
    val name: String,
    val originalName: String,
    val tag: String,
    val flagEmoji: String,
    val isBase: Boolean
)