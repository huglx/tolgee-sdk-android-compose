package cz.fit.cvut.feature.translation.domain.usecases

import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.data.api.dto.request.RelatedKeys
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.translations_context.domain.usecase.FindNeighborKeysUseCase
import kotlinx.coroutines.flow.first

internal class UpdateTranslationWithContextUseCase(
    private val translationsRepository: TranslationsRepository,
    private val neighborKeysUseCase: FindNeighborKeysUseCase
) {
    suspend operator fun invoke(
        keyName: String,
        keyId: Long,
        translations: Map<String, String>
    ): ResultWrapper<UpdateTranslationsContextResponse> {

        val neighborKeys = neighborKeysUseCase(keyName)

        val relatedKeys = neighborKeys.map { name ->
            RelatedKeys(name, null)
        }

        val updateTranslationContextRequest = UpdateTranslationContextRequest(
            name = keyName,
            relatedKeysInOrder = relatedKeys,
            translations = translations
        )

        val existingKey = translationsRepository.observeTranslationForKey(keyName).first()
        return if (existingKey == null) {
            translationsRepository.createTranslationWithContext(updateTranslationContextRequest)
        } else {
            translationsRepository.updateTranslationWithContext(
                updateTranslationContextRequest,
                keyId
            )
        }
    }
}