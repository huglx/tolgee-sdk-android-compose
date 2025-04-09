package cz.fit.cvut.feature.translation.data.source

import android.content.res.Resources.NotFoundException
import android.util.Log
import cz.fit.cvut.feature.translation.data.db.dao.TolgeeDao
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.translation.presentation.common.viewmodel.SingleTranslationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class TranslationsLocalSourceImpl(
    private val tolgeeDao: TolgeeDao,
    private val mapper: TolgeeEntityMapper
) : TranslationsLocalSource {
    override fun getAllKeys(): Flow<List<TolgeeKeyModel>> {
        return tolgeeDao.getAllKeys().map { keys ->
            keys.map { key ->
                val translations = tolgeeDao.getTranslationsForKey(key.keyId)
                val languages = tolgeeDao.getLanguagesForKey(key.keyId)
                mapper.toDomain(key, translations, languages)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeKeyByName(keyName: String): Flow<TolgeeKeyModel?> {
        return tolgeeDao.observeKeyByName(keyName).flatMapLatest { keyEntity ->
            if (keyEntity == null) {
                flowOf(null)
            } else {
                tolgeeDao.observeTranslationsForKey(keyEntity.keyId).map { translations ->
                    val languages = tolgeeDao.getLanguagesForKey(keyEntity.keyId)
                    mapper.toDomain(keyEntity, translations, languages)
                }
            }
        }
    }

    override suspend fun saveKey(key: TolgeeKeyModel) {
        val (keyEntity, _, translationEntities) = mapper.toEntity(key)
        tolgeeDao.insertKeys(listOf(keyEntity))
        tolgeeDao.insertTranslations(translationEntities)
    }

    override suspend fun saveKeys(keys: List<TolgeeKeyModel>) {
        val allEntities = keys.map { mapper.toEntity(it) }
        
        val beforeCount = tolgeeDao.getTranslationsCount()
        Log.d("TranslationsLocal", "Before batch insert: $beforeCount translations total")
        
        val allTranslations = allEntities.flatMap { (_, _, translations) -> translations }
        Log.d("TranslationsLocal", "Attempting to insert ${allTranslations.size} translations for ${keys.size} keys")
        
        tolgeeDao.insertCompleteData(
            keys = allEntities.map { it.first },
            translations = allTranslations,
            languages = allEntities.flatMap { it.second }.distinctBy { it.id }
        )
    }

    override suspend fun deleteAllKeys() {
        tolgeeDao.deleteAllKeys()
        tolgeeDao.deleteAllTranslations()
    }

    override suspend fun getKeyById(keyId: Long): TolgeeKeyModel {
        return tolgeeDao.findKeyById(keyId).let { keyEntity ->
            if (keyEntity == null) {
                throw IllegalArgumentException("Key with ID $keyId not found")
            }
            val translations = tolgeeDao.getTranslationsForKey(keyId)
            val languages = tolgeeDao.getLanguagesForKey(keyId)
            mapper.toDomain(keyEntity, translations, languages)
        } ?: throw IllegalArgumentException("Key with ID $keyId not found")
    }

    override suspend fun getKeyByNameOrCreateEmpty(keyName: String): TolgeeKeyModel {
        return tolgeeDao.findKeyByName(keyName).let {
            if (it == null) {
                return createEmptyTranslation(
                    keyName = keyName,
                    languages = tolgeeDao.getAllLanguages().first().map { lang ->
                        mapper.mapLanguageEntityToModel(lang)
                    },
                )
            }
            val translations = tolgeeDao.getTranslationsForKey(it.keyId)
            val languages = tolgeeDao.getLanguagesForKey(it.keyId)
            mapper.toDomain(it, translations, languages)
        }
    }

    private fun createEmptyTranslation(
        keyName: String,
        languages: List<TolgeeLanguageModel>,
    ): TolgeeKeyModel {
        val emptyTranslation = TolgeeKeyModel(
            keyName = keyName,
            translations = languages.associate { it.tag to TolgeeTranslationModel(0L, "", it) },
            keyId = 0L
        )

        return emptyTranslation
    }
} 