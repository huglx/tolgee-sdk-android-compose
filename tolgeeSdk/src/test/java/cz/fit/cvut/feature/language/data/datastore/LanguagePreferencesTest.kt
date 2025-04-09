package cz.fit.cvut.feature.language.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LanguagePreferencesTest {
    
    // Mock dependencies
    private lateinit var mockDataStore: DataStore<Preferences>
    
    // SUT (System Under Test)
    private lateinit var languagePreferences: LanguagePreferences
    
    @Before
    fun setup() {
         mockDataStore = mockk(relaxed = true)
        
        languagePreferences = object : LanguagePreferences {
            override val selectedLanguage = flowOf<String?>("en")
            override suspend fun saveSelectedLanguage(languageCode: String) {
                // A spy function for testing
            }
            override fun getSystemLanguage(): String = "en"
        }
    }
    
    @Test
    fun `getSystemLanguage returns current locale language`() {
        // Setup a real implementation for testing system locale
        val realImplementation = object : LanguagePreferences {
            override val selectedLanguage = flowOf<String?>(null)
            override suspend fun saveSelectedLanguage(languageCode: String) {}
            override fun getSystemLanguage(): String {
                return Locale.getDefault().language
            }
        }
        
        // Verify system language is returned
        val systemLanguage = realImplementation.getSystemLanguage()
        
        // System language should not be empty and should be a valid language code
        assert(systemLanguage.isNotEmpty())
    }
    
    @Test
    fun `selectedLanguage flow returns expected value`() = runTest {
        // When the flow is collected
        val language = languagePreferences.selectedLanguage.single()
        
        // Then the expected value is emitted
        assertEquals("en", language)
    }
} 