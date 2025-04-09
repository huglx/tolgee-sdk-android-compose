package cz.fit.cvut.feature.translation.data

import android.util.Log
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationNoContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSource

import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.core.common.utils.mapWithLanguages
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel

internal class TranslationsRepository(
    private val translationsRemoteSource: TranslationsRemoteSource,
    private val translationsLocalSource: TranslationsLocalSource,
    private val languageRepository: LanguageRepository,
) {
    /**
     * Get key names for the provided list of key IDs in the same order.
     * This function maps key IDs to their corresponding key names.
     *
     * @param keyIds List of key IDs to find names for
     * @return List of key names in the same order as the input key IDs.
     *         If a key ID is not found, its position will contain null.
     */
    private suspend fun getKeyNamesByKeyIds(keyIds: List<Long>): List<String?> {
        // Get all keys from the local database
        val allKeys = translationsLocalSource.getAllKeys().first()

        // Create a map of keyId to keyName for faster lookup
        val keyIdToNameMap = allKeys.associate { key -> key.keyId to key.keyName }

        // Map each keyId to its keyName in the same order
        return keyIds.map { keyId ->
            keyIdToNameMap[keyId]
        }
    }

    /**
     * Get key names for the provided list of key IDs in the same order.
     * Returns only non-null key names.
     *
     * @param keyIds List of key IDs to find names for
     * @return List of key names in the same order as the input key IDs,
     *         excluding any keys that weren't found
     */
    suspend fun getExistingKeyNamesByKeyIds(keyIds: List<Long>): List<String> {
        return getKeyNamesByKeyIds(keyIds).filterNotNull()
    }

    fun observeTranslationForKey(keyName: String): Flow<TolgeeKeyModel?> {
        return translationsLocalSource.observeKeyByName(keyName)
    }

    suspend fun getTranslations(languages: List<TolgeeLanguageModel>): ResultWrapper<List<TolgeeKeyModel>> {
        val localTranslations = translationsLocalSource.getAllKeys().first()
        
        if (localTranslations.isEmpty()) {
            return fetchAndCacheTranslations(languages)
        }
        
        return ResultWrapper.Success(localTranslations)
    }

    suspend fun fetchAndCacheTranslations(languages: List<TolgeeLanguageModel>): ResultWrapper<List<TolgeeKeyModel>> {
        val allTranslations = mutableListOf<TolgeeKeyModel>()
        var currentPage = 0
        var totalPages = 1

        try {
            while (currentPage < totalPages) {
                val response = translationsRemoteSource.getTranslations(currentPage, languages.joinToString(",") { it.tag })
                val keysWithTranslations = response.toModels().mapWithLanguages(languages)
                
                allTranslations.addAll(keysWithTranslations)
                translationsLocalSource.saveKeys(keysWithTranslations)

                currentPage++
                totalPages = response.embedded.totalPages
            }
        } catch (e: Exception) {
            Log.e("TranslationsRepo", "Error fetching translations", e)
            return ResultWrapper.Error(message = e.message ?: "", throwable = e)
        }

        return ResultWrapper.Success(allTranslations)
    }

    fun getTranslationsFlow(): Flow<List<TolgeeKeyModel>> = translationsLocalSource.getAllKeys()

    suspend fun updateTranslationNoContext(
        updateTranslationNoContextRequest: UpdateTranslationNoContextRequest
    ): ResultWrapper<TolgeeKeyModel> {
        return try {
            ResultWrapper.Success(
                translationsRemoteSource.updateTranslationNoContext(updateTranslationNoContextRequest).toModel()
            )
        } catch (e: Exception) {
            Log.e("TranslationsRepo", "Error updating translation", e)
            ResultWrapper.Error(message = e.message ?: "", throwable = e)
        }
    }

    suspend fun updateTranslationWithContext(
        updateTranslationContextRequest: UpdateTranslationContextRequest,
        keyId: Long,
    ): ResultWrapper<UpdateTranslationsContextResponse> {
        return try {
            val response = translationsRemoteSource.updateTranslationWithContext(
                updateTranslationContextRequest,
                keyId
            )
            
            // Update translation in database
            observeTranslationForKey(response.keyName)
                .first()
                ?.let { existingKey ->
                    val updatedTranslations = existingKey.translations.toMutableMap()
                    
                    response.translations.forEach { (languageTag, translationResponse) ->
                        updatedTranslations[languageTag]?.let { translation ->
                            updatedTranslations[languageTag] = translation.copy(
                                text = translationResponse.text
                            )
                        }
                    }
                    
                    val updatedKey = existingKey.copy(
                        translations = updatedTranslations
                    )
                    translationsLocalSource.saveKey(updatedKey)
                }
            
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            Log.e("TranslationsRepo", "Error updating translation with context", e)
            ResultWrapper.Error(message = e.message ?: "", throwable = e)
        }
    }

    suspend fun createTranslationWithContext(
        updateTranslationContextRequest: UpdateTranslationContextRequest,
    ): ResultWrapper<UpdateTranslationsContextResponse> {
        return try {
            val response = translationsRemoteSource.createTranslationWithContext(
                updateTranslationContextRequest
            )

            val languages = languageRepository.getLanguages()
            val translations = response.translations.mapValues { (languageTag, translationResponse) ->
                TolgeeTranslationModel(
                    text = translationResponse.text,
                    language = languages.find { it.tag == languageTag },
                    id = translationResponse.id
                )
            }
            
            val newKey = TolgeeKeyModel(
                keyName = response.keyName,
                translations = translations,
                keyId = response.keyId
            )
            translationsLocalSource.saveKey(newKey)
            
            ResultWrapper.Success(response)
        } catch (e: Exception) {
            Log.e("TranslationsRepo", "Error creating translation with context", e)
            ResultWrapper.Error(message = e.message ?: "", throwable = e)
        }
    }

    suspend fun refreshTranslations(languages: List<TolgeeLanguageModel>): ResultWrapper<List<TolgeeKeyModel>> {
        return fetchAndCacheTranslations(languages)
    }

    suspend fun clearLocalCache() {
        translationsLocalSource.deleteAllKeys()
    }

    suspend fun getKeyById(keyId: Long): ResultWrapper<TolgeeKeyModel> {
        return try {
            val key = translationsLocalSource.getKeyById(keyId)
            ResultWrapper.Success(key)
        } catch (exception: Exception) {
            Log.e("TranslationsRepo", "Error getting key by ID", exception)
            ResultWrapper.Error(message = exception.message ?: "", throwable = exception)
        }
    }

    suspend fun getKeyByName(keyName: String): ResultWrapper<TolgeeKeyModel> {
        return try {
            val key = translationsLocalSource.getKeyByNameOrCreateEmpty(keyName)
            ResultWrapper.Success(key)
        } catch (exception: Exception) {
            Log.e("TranslationsRepo", "Error getting key by name", exception)
            ResultWrapper.Error(message = exception.message ?: "", throwable = exception)
        }
    }
}