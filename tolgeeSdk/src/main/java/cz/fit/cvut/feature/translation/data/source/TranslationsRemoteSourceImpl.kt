package cz.fit.cvut.feature.translation.data.source

import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationNoContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.TolgeeKeyResponse
import cz.fit.cvut.feature.translation.data.api.TranslationsApiDescription
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationNoContextResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse

internal class TranslationsRemoteSourceImpl(
    private val apiDescription: TranslationsApiDescription,
): TranslationsRemoteSource {
    override suspend fun getTranslations(currentPage: Int, languages: String): TolgeeKeyResponse {
        return apiDescription.getTranslations(currentPage, languages)
    }

    override suspend fun updateTranslationNoContext(
        updateTranslationNoContextRequest: UpdateTranslationNoContextRequest
    ): UpdateTranslationNoContextResponse {
        return apiDescription.updateTranslationNoContext(updateTranslationNoContextRequest)
    }

    override suspend fun updateTranslationWithContext(
        updateTranslationContextRequest: UpdateTranslationContextRequest,
        keyId: Long,
    ): UpdateTranslationsContextResponse {
        return apiDescription.updateTranslationWithContext(updateTranslationContextRequest, keyId)
    }

    override suspend fun createTranslationWithContext(request: UpdateTranslationContextRequest): UpdateTranslationsContextResponse {
        return apiDescription.createTranslationWithContext(request)
    }

}