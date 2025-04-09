package cz.fit.cvut.feature.language.data.api

import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse
import retrofit2.http.GET

internal interface LanguageApiDescription {
    @GET("v2/projects/languages")
    suspend fun getLanguages(): TolgeeLanguagesResponse
} 