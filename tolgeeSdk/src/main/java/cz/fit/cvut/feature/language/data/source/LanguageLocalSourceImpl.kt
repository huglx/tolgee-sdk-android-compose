package cz.fit.cvut.feature.language.data.source

import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.language.data.db.dao.LanguageDao
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LanguageLocalSourceImpl(
    private val tolgeeDao: LanguageDao,
    private val mapper: LanguageEntityMapper
) : LanguageLocalSource {

    override fun getAllLanguages(): Flow<List<TolgeeLanguageModel>> {
        return tolgeeDao.getAllLanguages().map { entities ->
            entities.map { mapper.mapLanguageEntityToModel(it) }
        }
    }

    override suspend fun saveLanguages(languages: List<TolgeeLanguageModel>) {
        val entities = languages.map { mapper.mapLanguageModelToEntity(it) }
        tolgeeDao.insertLanguages(entities)
    }

    override suspend fun deleteAllLanguages() {
        tolgeeDao.deleteAllLanguages()
    }
} 