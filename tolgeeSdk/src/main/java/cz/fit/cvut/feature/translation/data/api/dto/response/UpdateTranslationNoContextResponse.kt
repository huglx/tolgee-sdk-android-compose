package cz.fit.cvut.feature.translation.data.api.dto.response

import com.google.gson.annotations.SerializedName
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel

internal data class UpdateTranslationNoContextResponse(
    @SerializedName("keyId")
    var keyId: Long,
    @SerializedName("keyName")
    var keyName: String,
    @SerializedName("translations")
    var translations: Map<String, Translation>
) {
    fun toModel(): TolgeeKeyModel {
        return TolgeeKeyModel(
            keyId = keyId,
            keyName = keyName,
            translations = translations.mapValues { TolgeeTranslationModel(it.value.id, it.value.text, null) }
        )
    }
}
