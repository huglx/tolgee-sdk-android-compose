package cz.fit.cvut.feature.translation.data.db.dao

import android.util.Log
import androidx.room.*
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity
import cz.fit.cvut.feature.translation.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TolgeeDao {
    @Query("SELECT * FROM tolgee_keys")
    fun getAllKeys(): Flow<List<TolgeeKeyEntity>>

    @Query("SELECT * FROM tolgee_keys WHERE keyName = :keyName")
    fun observeKeyByName(keyName: String): Flow<TolgeeKeyEntity?>

    @Query("SELECT * FROM tolgee_translations WHERE keyId = :keyId")
    suspend fun getTranslationsForKey(keyId: Long): List<TolgeeTranslationEntity>

    @Query("SELECT * FROM tolgee_languages WHERE languageId IN (SELECT DISTINCT languageId FROM tolgee_translations WHERE keyId = :keyId)")
    suspend fun getLanguagesForKey(keyId: Long): List<TolgeeLanguageEntity>

    @Query("SELECT COUNT(*) FROM tolgee_translations")
    suspend fun getTranslationsCount(): Int

    @Query("SELECT * FROM tolgee_keys WHERE keyId = :keyId")
    suspend fun findKeyById(keyId: Long): TolgeeKeyEntity?

    @Query("SELECT * FROM tolgee_keys WHERE keyName = :keyName")
    suspend fun findKeyByName(keyName: String): TolgeeKeyEntity?

    @Upsert
    suspend fun insertKeys(keys: List<TolgeeKeyEntity>)

    @Upsert
    suspend fun insertLanguages(languages: List<TolgeeLanguageEntity>)

    @Upsert
    suspend fun insertTranslations(translations: List<TolgeeTranslationEntity>): List<Long>

    @Query("DELETE FROM tolgee_keys")
    suspend fun deleteAllKeys()

    @Query("DELETE FROM tolgee_translations")
    suspend fun deleteAllTranslations()

    @Query("DELETE FROM tolgee_languages")
    suspend fun deleteAllLanguages()

    @Query("DELETE FROM tolgee_translations WHERE keyId = :keyId")
    suspend fun deleteTranslationsForKey(keyId: Long)

    @Query("SELECT * FROM tolgee_languages")
    fun getAllLanguages(): Flow<List<TolgeeLanguageEntity>>

    @Transaction
    suspend fun insertCompleteData(
        keys: List<TolgeeKeyEntity>,
        translations: List<TolgeeTranslationEntity>,
        languages: List<TolgeeLanguageEntity>
    ) {
        insertLanguages(languages)
        insertKeys(keys)
        val insertedIds = insertTranslations(translations)
        
        if (insertedIds.size != translations.size) {
            Log.w("TolgeeDao", """
                Mismatch in translations insertion:
                - Expected: ${translations.size}
                - Actual: ${insertedIds.size}
                This might indicate some translations were not inserted properly.
            """.trimIndent())
        }
    }

    @Query("SELECT * FROM tolgee_translations WHERE keyId = :keyId AND languageId = :languageId")
    suspend fun getTranslationByKeyAndLanguage(keyId: Long, languageId: String): TolgeeTranslationEntity?

    @Query("SELECT * FROM tolgee_translations WHERE keyId = :keyId")
    fun observeTranslationsForKey(keyId: Long): Flow<List<TolgeeTranslationEntity>>

    @Query("SELECT * FROM tolgee_languages WHERE languageId IN (SELECT DISTINCT languageId FROM tolgee_translations WHERE keyId = :keyId)")
    fun observeLanguagesForKey(keyId: Long): Flow<List<TolgeeLanguageEntity>>
} 