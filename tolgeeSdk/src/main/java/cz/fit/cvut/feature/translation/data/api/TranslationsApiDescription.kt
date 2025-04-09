package cz.fit.cvut.feature.translation.data.api

import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationNoContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.TolgeeKeyResponse
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationNoContextResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TranslationsApiDescription {
    @GET("v2/projects/translations?size=20&sort=keyId,asc")
    suspend fun getTranslations(
        @Query("page") currentPage: Int = 0,
        @Query("languages") currentLanguage: String = "en"
    ): TolgeeKeyResponse

    @PUT("v2/projects/translations")
    suspend fun updateTranslationNoContext(
        @Body request: UpdateTranslationNoContextRequest
    ): UpdateTranslationNoContextResponse

    @PUT("v2/projects/keys/{id}/complex-update")
    suspend fun updateTranslationWithContext(
        @Body request: UpdateTranslationContextRequest,
        @Path("id") keyId: Long,
    ): UpdateTranslationsContextResponse

    @POST("v2/projects/keys/create")
    suspend fun createTranslationWithContext(
        @Body request: UpdateTranslationContextRequest
    ): UpdateTranslationsContextResponse
}