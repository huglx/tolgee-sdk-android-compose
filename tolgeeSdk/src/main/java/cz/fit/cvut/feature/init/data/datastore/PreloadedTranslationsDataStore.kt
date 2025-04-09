package cz.fit.cvut.feature.init.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Interface for preloaded translations preferences
 * Defines the contract for managing preloaded translations state
 */
internal interface PreloadedTranslationsPreferences {
    /**
     * Get the state whether translations were preloaded
     * @return Flow of boolean indicating if translations were preloaded
     */
    val wasPreloaded: Flow<Boolean>
    
    /**
     * Save the preloaded state
     * @param preloaded Boolean indicating if translations were preloaded
     */
    suspend fun savePreloadedState(preloaded: Boolean)
}

/**
 * Implementation of PreloadedTranslationsPreferences for managing preloaded translations state
 * This class handles the details of storing and retrieving preloaded state
 * from the DataStore
 */
internal class PreloadedTranslationsPreferencesImpl(
    private val dataStore: DataStore<Preferences>
) : PreloadedTranslationsPreferences {
    
    companion object {
        // Preference keys
        private val WAS_PRELOADED_KEY = booleanPreferencesKey("was_preloaded")
    }
    
    override val wasPreloaded: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[WAS_PRELOADED_KEY] ?: false
    }
    
    override suspend fun savePreloadedState(preloaded: Boolean) {
        dataStore.edit { preferences ->
            preferences[WAS_PRELOADED_KEY] = preloaded
        }
    }
} 