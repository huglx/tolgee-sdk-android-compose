package cz.fit.cvut.feature.translation.data.api.dto.response

import com.google.gson.annotations.SerializedName

data class UpdateTranslationsContextResponse(
    @SerializedName("description")
    var description: String?,
    @SerializedName("id")
    var keyId: Long,
    @SerializedName("name")
    var keyName: String,
    @SerializedName("translations")
    var translations: Map<String, TranslationResponse>
)

data class TranslationResponse(
    @SerializedName("id")
    var id: Long,
    @SerializedName("text")
    var text: String
)
