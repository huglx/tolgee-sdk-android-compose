package cz.fit.cvut.feature.language.data.source

import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import kotlinx.coroutines.flow.Flow

internal interface LanguageLocalSource {
    fun getAllLanguages(): Flow<List<TolgeeLanguageModel>>
    
    suspend fun saveLanguages(languages: List<TolgeeLanguageModel>)

    suspend fun deleteAllLanguages()
} 