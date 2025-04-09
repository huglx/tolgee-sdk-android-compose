package cz.fit.cvut.feature.language.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import cz.fit.cvut.feature.language.data.db.entity.TolgeeLanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LanguageDao {
    @Query("SELECT * FROM tolgee_languages")
    fun getAllLanguages(): Flow<List<TolgeeLanguageEntity>>

    @Upsert
    suspend fun insertLanguages(languages: List<TolgeeLanguageEntity>)

    @Query("DELETE FROM tolgee_languages")
    suspend fun deleteAllLanguages()
} 