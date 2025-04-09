package cz.fit.cvut.feature.translation.domain.usecases

import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationNoContextRequest
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel

internal class UpdateTranslationNoContextUseCase(
    private val translationsRepository: TranslationsRepository
) {
    suspend operator fun invoke(
        keyName: String,
        newTranslations: Map<String, String>
    ): ResultWrapper<TolgeeKeyModel> {
        val updateTranslationNoContextRequest = UpdateTranslationNoContextRequest(
            key = keyName,
            translations = newTranslations
        )
        return translationsRepository.updateTranslationNoContext(updateTranslationNoContextRequest)
    }
}