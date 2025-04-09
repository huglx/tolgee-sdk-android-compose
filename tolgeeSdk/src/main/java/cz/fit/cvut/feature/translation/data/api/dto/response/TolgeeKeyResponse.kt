package cz.fit.cvut.feature.translation.data.api.dto.response

import com.google.gson.annotations.SerializedName
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel

internal data class TolgeeKeyResponse(
    @SerializedName("_embedded") var embedded: EmbeddedKeys
) {
    fun toModels(): List<TolgeeKeyModel> {
        return embedded.keys.map { key ->
            TolgeeKeyModel(
                keyId = key.keyId,
                keyName = key.keyName,
                translations = key.translations.mapValues { TolgeeTranslationModel(id = it.value.id, it.value.text, null) }
            )
        }
    }

}

internal data class EmbeddedKeys(
    @SerializedName("keys") var keys: List<Key>,
    @SerializedName("totalPages") var totalPages: Int,
    @SerializedName("currentPage") var currentPage: Int,
)

internal data class Key(
    @SerializedName("keyId") var keyId: Long,
    @SerializedName("keyName") var keyName: String,
    @SerializedName("translations") var translations: Map<String, Translation>
)

internal data class Translation(
    @SerializedName("id") var id: Long,
    @SerializedName("text") var text: String,
)