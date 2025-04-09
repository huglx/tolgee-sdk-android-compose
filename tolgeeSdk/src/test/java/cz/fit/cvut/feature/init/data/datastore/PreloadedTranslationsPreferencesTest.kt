package cz.fit.cvut.feature.init.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
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

class PreloadedTranslationsPreferencesTest {
    
    // Mock dependencies
    private lateinit var mockDataStore: DataStore<Preferences>
    
    // System Under Test
    private lateinit var preloadedTranslationsPreferences: PreloadedTranslationsPreferences
    private val wasPreloadedKey = booleanPreferencesKey("was_preloaded")
    
    @Before
    fun setup() {
        mockDataStore = mockk(relaxed = true)
        
        // Create a mock implementation for initial test setup
        preloadedTranslationsPreferences = object : PreloadedTranslationsPreferences {
            override val wasPreloaded = flowOf(false)
            override suspend fun savePreloadedState(preloaded: Boolean) {
                // Spy function for testing
            }
        }
    }
    
    @Test
    fun `wasPreloaded flow returns expected value when not preloaded`() = runTest {
        // Given
        val preferences = preferencesOf()
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(preferences)
        
        // When
        val impl = PreloadedTranslationsPreferencesImpl(dataStore)
        val result = impl.wasPreloaded.single()
        
        // Then
        assertEquals(false, result)
    }
    
    @Test
    fun `wasPreloaded flow returns expected value when preloaded`() = runTest {
        // Given
        val preferences = preferencesOf(wasPreloadedKey to true)
        val dataStore = mockk<DataStore<Preferences>>()
        every { dataStore.data } returns flowOf(preferences)
        
        // When
        val impl = PreloadedTranslationsPreferencesImpl(dataStore)
        val result = impl.wasPreloaded.single()
        
        // Then
        assertEquals(true, result)
    }
    
    @Test
    fun `savePreloadedState updates preferences correctly`() = runTest {
        // Given
        val dataStore = mockk<DataStore<Preferences>>()
        coEvery { dataStore.updateData(any()) } returns mockk()
        val preferences = preferencesOf(wasPreloadedKey to false)
        every { dataStore.data } returns flowOf(preferences)


        // When
        val impl = PreloadedTranslationsPreferencesImpl(dataStore)
        impl.savePreloadedState(true)
        
        // Then
        coVerify { dataStore.updateData(any()) }
    }
} 