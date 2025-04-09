package cz.fit.cvut.feature.language.data.api.dto.response

import com.google.gson.annotations.SerializedName
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel

internal data class TolgeeLanguagesResponse(
    @SerializedName("_embedded") val embedded: EmbeddedLanguages
) {
    fun toModels(): List<TolgeeLanguageModel> {
        return embedded.languages.map { language ->
            TolgeeLanguageModel(
                id = language.id,
                name = language.name,
                originalName = language.originalName,
                tag = language.tag,
                flagEmoji = language.flagEmoji,
                isBase = language.isBase
            )
        }
    }
}

internal data class EmbeddedLanguages(
    @SerializedName("languages") val languages: List<TolgeeLanguageDto>
)

internal data class TolgeeLanguageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("originalName") val originalName: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("flagEmoji") val flagEmoji: String,
    @SerializedName("base") val isBase: Boolean
)
