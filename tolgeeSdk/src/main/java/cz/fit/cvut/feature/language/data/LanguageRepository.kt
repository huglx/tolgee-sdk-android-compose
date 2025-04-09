package cz.fit.cvut.feature.language.data

import android.util.Log
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferences
import cz.fit.cvut.feature.language.data.source.LanguageLocalSource
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "LanguageRepository"

/**
 * Repository for language operations, follows the Repository pattern
 * to provide a clean API over different data sources: remote, local and preferences
 */
internal class LanguageRepository(
    private val languageRemoteSource: LanguageRemoteSource,
    private val languageLocalSource: LanguageLocalSource,
    private val langPreferences: LanguagePreferences
) {
    companion object {
        // Default to English as fallback
        private const val DEFAULT_LANGUAGE = "en"
    }
    
    /**
     * The currently selected language.
     * This flow is sourced directly from the DataStore with a fallback to default language
     */
    val selectedLanguage: Flow<String> = langPreferences.selectedLanguage.map { 
        it ?: DEFAULT_LANGUAGE 
    }
    
    /**
     * Set the selected language and save it to DataStore
     * This is a suspend function that should be called from a coroutine
     */
    suspend fun setSelectedLanguage(language: String) {
        Log.d(TAG, "Setting selected language to: $language")
        langPreferences.saveSelectedLanguage(language)
    }

    /**
     * Initialize the system language based on available languages
     * This should be called during app initialization by the InitRepository
     * 
     * @param languages List of available languages to select from
     * @return ResultWrapper with the chosen language
     */
    suspend fun initializeLanguage(languages: List<TolgeeLanguageModel>): ResultWrapper<String> {
        Log.d(TAG, "Initializing language with ${languages.size} available languages")
        
        return try {
            val currentLanguage = langPreferences.selectedLanguage.first()
            
            if (currentLanguage != null) {
                Log.d(TAG, "Language already set to: $currentLanguage")
                return ResultWrapper.Success(currentLanguage)
            }
            
            // Get system language
            val systemLanguage = langPreferences.getSystemLanguage()
            Log.d(TAG, "System language detected: $systemLanguage")
            
            // Log available languages for debugging
            languages.forEach { 
                Log.d(TAG, "Available language: ${it.tag} (base: ${it.isBase})")
            }
            
            // Determine language to use
            val languageToSet = if (languages.any { it.tag.lowercase() == systemLanguage.lowercase() }) {
                Log.d(TAG, "Using system language (supported): $systemLanguage")
                systemLanguage
            } else {
                val baseLanguage = languages.firstOrNull { it.isBase }?.tag
                val selectedLang = baseLanguage ?: DEFAULT_LANGUAGE
                Log.d(TAG, "System language not supported, using: $selectedLang")
                selectedLang
            }
            
            Log.d(TAG, "Saving initial language preference: $languageToSet")
            langPreferences.saveSelectedLanguage(languageToSet)
            
            ResultWrapper.Success(languageToSet)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing language: ${e.message}", e)
            langPreferences.saveSelectedLanguage(DEFAULT_LANGUAGE)
            ResultWrapper.Error(message = "Failed to initialize language: ${e.message}", throwable = e)
        }
    }

    /**
     * Get all available languages
     */
    suspend fun getLanguages(): List<TolgeeLanguageModel> {
        val languages = findLanguages()
        if (languages is ResultWrapper.Success) {
            return languages.data
        } else {
            throw (languages as ResultWrapper.Error).throwable!!
        }
    }
    
    /**
     * Find all available languages, first checking local source, then remote
     */
    suspend fun findLanguages(): ResultWrapper<List<TolgeeLanguageModel>> {
        Log.d(TAG, "Finding languages (local first)")
        val localLanguages = languageLocalSource.getAllLanguages().first()
        
        if (localLanguages.isNotEmpty()) {
            Log.d(TAG, "Found ${localLanguages.size} languages in local source")
            return ResultWrapper.Success(localLanguages)
        }
        
        Log.d(TAG, "No local languages found, fetching from remote")
        return fetchAndCacheLanguages()
    }
    
    /**
     * Fetch languages from remote API and cache them locally
     */
    suspend fun fetchAndCacheLanguages(): ResultWrapper<List<TolgeeLanguageModel>> {
        Log.d(TAG, "Fetching languages from remote API")
        return try {
            val response = languageRemoteSource.getLanguages()
            val sortedBaseLangIsFirst = response.toModels().sortedBy {
                !it.isBase
            }
            Log.d(TAG, "Fetched ${sortedBaseLangIsFirst.size} languages from remote")
            languageLocalSource.saveLanguages(sortedBaseLangIsFirst)
            ResultWrapper.Success(sortedBaseLangIsFirst)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching languages: ${e.message}", e)
            ResultWrapper.Error(message = e.message ?: "", throwable = e)
        }
    }

    /**
     * Clear cached languages
     */
    suspend fun clearLanguagesCache() {
        Log.d(TAG, "Clearing language cache")
        languageLocalSource.deleteAllLanguages()
    }
} 