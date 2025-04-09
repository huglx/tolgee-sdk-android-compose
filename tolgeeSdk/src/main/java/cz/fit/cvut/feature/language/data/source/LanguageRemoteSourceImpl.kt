package cz.fit.cvut.feature.language.data.source

import cz.fit.cvut.feature.language.data.api.LanguageApiDescription
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse

internal class LanguageRemoteSourceImpl(
    private val api: LanguageApiDescription
) : LanguageRemoteSource {
    override suspend fun getLanguages(): TolgeeLanguagesResponse {
        return api.getLanguages()
    }
} 