package cz.fit.cvut.feature.init.data.source

import cz.fit.cvut.core.common.utils.ResultWrapper

/**
 * Interface for reading preloaded translations from assets
 */
internal interface PreloadedTranslationsDataSource {
    /**
     * Check if preloaded translations are available
     */
    fun isAvailable(): Boolean

    /**
     * Get SQL statements for preloaded translations
     */
    suspend fun getSqlStatements(): ResultWrapper<List<String>>
}