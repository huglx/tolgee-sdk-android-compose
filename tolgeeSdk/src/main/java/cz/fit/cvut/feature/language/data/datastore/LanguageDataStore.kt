package cz.fit.cvut.feature.language.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * Interface for language-specific preferences
 * Defines the contract for language management operations
 */
internal interface LanguagePreferences {
    /**
     * Get the selected language preference
     * @return Flow of language code (e.g., "en", "cs")
     */
    val selectedLanguage: Flow<String?>
    
    /**
     * Save the selected language
     * @param languageCode Language code to save (e.g., "en", "cs")
     */
    suspend fun saveSelectedLanguage(languageCode: String)
    
    /**
     * Get the device's system language
     * @return System language code
     */
    fun getSystemLanguage(): String
}

/**
 * Implementation of LanguagePreferences for managing language settings
 * This class handles the details of storing and retrieving language preferences
 * from the DataStore
 */
class LanguagePreferencesImpl(
    private val dataStore: DataStore<Preferences>
) : LanguagePreferences {
    
    companion object {
        // Preference keys
        private val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }
    
    override val selectedLanguage: Flow<String?> = dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE_KEY]
    }
    
    override suspend fun saveSelectedLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE_KEY] = languageCode
        }
    }
    
    override fun getSystemLanguage(): String {
        val locale = Locale.getDefault()
        return locale.language
    }
} 