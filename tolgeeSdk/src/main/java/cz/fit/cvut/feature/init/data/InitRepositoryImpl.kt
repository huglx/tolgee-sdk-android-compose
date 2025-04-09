package cz.fit.cvut.feature.init.data

import android.util.Log
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.feature.init.data.source.PreloadedTranslationsDataSource
import cz.fit.cvut.feature.init.domain.InitRepository
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.core.common.utils.InitStateManager
import cz.fit.cvut.feature.init.data.datastore.PreloadedTranslationsPreferences
import kotlinx.coroutines.flow.first

private const val TAG = "InitRepository"

internal class InitRepositoryImpl(
    private val languageRepository: LanguageRepository,
    private val translationsRepository: TranslationsRepository,
    private val preloadedTranslationsDataSource: PreloadedTranslationsDataSource,
    private val database: TolgeeDB,
    private val initStateManager: InitStateManager,
    private val preloadedTranslationsPreferences: PreloadedTranslationsPreferences
) : InitRepository {

    override suspend fun initFetching(): ResultWrapper<Unit> {
        Log.d(TAG, "Starting initialization process")

        return try {
            // Check if already preloaded
            val alreadyPreloaded = preloadedTranslationsPreferences.wasPreloaded.first()

            val result = when {
                alreadyPreloaded -> {
                    Log.d(TAG, "Translations already preloaded before, updating from network")
                    initFromNetwork()
                    ResultWrapper.Success(Unit)
                }
                preloadedTranslationsDataSource.isAvailable() -> {
                    handlePreloadedTranslations()
                }
                else -> {
                    Log.d(TAG, "No preloaded translations available, initializing from network")
                    initFromNetwork()
                }
            }

            handleResult(result)
        } catch (e: Exception) {
            initStateManager.setError(e)
            Log.e(TAG, "Initialization failed", e)
            ResultWrapper.Error("Initialization failed", e)
        }
    }

    private suspend fun handlePreloadedTranslations(): ResultWrapper<Unit> {
        Log.d(TAG, "Preloaded translations are available")
        val preloadResult = initFromPreloadedData()

        return if (preloadResult is ResultWrapper.Success) {
            Log.d(TAG, "Preloaded data initialization successful")
            // Mark as preloaded
            preloadedTranslationsPreferences.savePreloadedState(true)
            initializeLanguageAfterPreload()
            preloadResult
        } else {
            Log.e(TAG, "Failed to initialize from preloaded data: ${(preloadResult as ResultWrapper.Error).message}")
            preloadResult
        }
    }

    private suspend fun handleResult(result: ResultWrapper<Unit>): ResultWrapper<Unit> {
        return when (result) {
            is ResultWrapper.Success -> {
                initStateManager.setInitialized()
                result
            }
            is ResultWrapper.Error -> {
                initStateManager.setError(result.throwable ?: Exception(result.message))
                initFromNetwork()
            }
        }
    }


    /**
     * Initialize language preferences after preloaded data is loaded
     */
    private suspend fun initializeLanguageAfterPreload(): ResultWrapper<String> {
        Log.d(TAG, "Initializing language after preloaded data")
        val languages = try {
            val langs = languageRepository.getLanguages()
            Log.d(TAG, "Got ${langs.size} languages for initialization")
            langs
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get languages for initialization: ${e.message}", e)
            return ResultWrapper.Error("Failed to get languages for initialization: ${e.message}", e)
        }
        
        val result = languageRepository.initializeLanguage(languages)
        if (result is ResultWrapper.Success) {
            Log.d(TAG, "Language initialized to: ${result.data}")
        } else {
            Log.e(TAG, "Failed to initialize language: ${(result as ResultWrapper.Error).message}")
        }
        return result
    }

    /**
     * Initialize using preloaded data from assets
     */
    private suspend fun initFromPreloadedData(): ResultWrapper<Unit> {
        Log.d(TAG, "Initializing from preloaded data")
        // Clear caches first
        languageRepository.clearLanguagesCache()
        translationsRepository.clearLocalCache()

        when (val sqlResult = preloadedTranslationsDataSource.getSqlStatements()) {
            is ResultWrapper.Error -> {
                Log.e(TAG, "Failed to get SQL statements: ${sqlResult.message}")
                return ResultWrapper.Error(
                    message = "Failed to get SQL statements: ${sqlResult.message}",
                    throwable = sqlResult.throwable
                )
            }
            is ResultWrapper.Success -> {
                Log.d(TAG, "Got ${sqlResult.data.size} SQL statements to execute")
                // Execute SQL statements in a transaction
                try {
                    database.openHelper.writableDatabase.beginTransaction()

                    for (statement in sqlResult.data) {
                        if (statement.isNotEmpty()) {
                            database.openHelper.writableDatabase.execSQL(statement)
                        }
                    }

                    database.openHelper.writableDatabase.setTransactionSuccessful()
                    Log.d(TAG, "SQL statements executed successfully")
                    return ResultWrapper.Success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to execute SQL statements: ${e.message}", e)
                    return ResultWrapper.Error(
                        message = "Failed to execute SQL statements: ${e.message}",
                        throwable = e
                    )
                } finally {
                    database.openHelper.writableDatabase.endTransaction()
                }
            }
        }
    }

    /**
     * Initialize by fetching data from network
     */
    private suspend fun initFromNetwork(): ResultWrapper<Unit> {
        Log.d(TAG, "Initializing from network")
        return when (val languagesResult = languageRepository.fetchAndCacheLanguages()) {
            is ResultWrapper.Error -> {
                Log.e(TAG, "Failed to fetch languages: ${languagesResult.message}")
                ResultWrapper.Error(
                    message = "Failed to fetch languages: ${languagesResult.message}",
                    throwable = languagesResult.throwable
                )
            }
            is ResultWrapper.Success -> {
                Log.d(TAG, "Languages fetched successfully, initializing language")
                // Initialize language using fetched languages
                val languages = languagesResult.data
                val langInitResult = languageRepository.initializeLanguage(languages)
                
                if (langInitResult is ResultWrapper.Error) {
                    Log.e(TAG, "Failed to initialize language: ${langInitResult.message}")
                    return ResultWrapper.Error(
                        message = "Failed to initialize language: ${langInitResult.message}",
                        throwable = langInitResult.throwable
                    )
                } else {
                    Log.d(TAG, "Language initialized to: ${(langInitResult as ResultWrapper.Success).data}")
                }
                
                // Get translations from network
                Log.d(TAG, "Fetching translations for initialized language")
                when (val translationsResult = translationsRepository.fetchAndCacheTranslations(languages)) {
                    is ResultWrapper.Error -> {
                        Log.e(TAG, "Failed to fetch translations: ${translationsResult.message}")
                        ResultWrapper.Error(
                            message = "Failed to fetch translations: ${translationsResult.message}",
                            throwable = translationsResult.throwable
                        )
                    }
                    is ResultWrapper.Success -> {
                        Log.d(TAG, "Translations fetched successfully, initialization complete")
                        ResultWrapper.Success(Unit)
                    }
                }
            }
        }
    }
}