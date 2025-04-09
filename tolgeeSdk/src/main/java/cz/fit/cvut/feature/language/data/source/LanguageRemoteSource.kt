package cz.fit.cvut.feature.language.data.source

import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse

internal interface LanguageRemoteSource {
    suspend fun getLanguages(): TolgeeLanguagesResponse
} 