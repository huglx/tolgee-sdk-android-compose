package cz.fit.cvut.feature.translation.data.source

import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import kotlinx.coroutines.flow.Flow

internal interface TranslationsLocalSource {
    fun getAllKeys(): Flow<List<TolgeeKeyModel>>

    fun observeKeyByName(keyName: String): Flow<TolgeeKeyModel?>
    
    suspend fun saveKey(key: TolgeeKeyModel)
    
    suspend fun saveKeys(keys: List<TolgeeKeyModel>)

    suspend fun deleteAllKeys()

    suspend fun getKeyById(keyId: Long): TolgeeKeyModel

    suspend fun getKeyByNameOrCreateEmpty(keyName: String): TolgeeKeyModel
}