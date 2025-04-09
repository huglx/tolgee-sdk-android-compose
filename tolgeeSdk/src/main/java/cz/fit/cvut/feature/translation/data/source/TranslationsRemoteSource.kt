package cz.fit.cvut.feature.translation.data.source

import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationNoContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.TolgeeKeyResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationNoContextResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse

internal interface TranslationsRemoteSource {
    suspend fun getTranslations(currentPage: Int, languages: String): TolgeeKeyResponse

    suspend fun updateTranslationNoContext(
        updateTranslationNoContextRequest: UpdateTranslationNoContextRequest
    ): UpdateTranslationNoContextResponse

    suspend fun updateTranslationWithContext(
        updateTranslationContextRequest: UpdateTranslationContextRequest,
        keyId: Long
    ): UpdateTranslationsContextResponse

    suspend fun createTranslationWithContext(
        request: UpdateTranslationContextRequest
    ): UpdateTranslationsContextResponse
}