package cz.fit.cvut.feature.language.data

import app.cash.turbine.test
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.language.data.api.dto.response.EmbeddedLanguages
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguageDto
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferences
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import cz.fit.cvut.feature.language.data.source.LanguageLocalSourceImpl
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class LanguageRepositoryIntegrationTest : TestTolgeeDB() {

    private lateinit var repository: LanguageRepository
    private lateinit var remoteSource: LanguageRemoteSource
    private lateinit var languagePreferences: LanguagePreferences
    private val mapper = LanguageEntityMapper()

    private val testLanguages = listOf(
        TolgeeLanguageModel(
            id = 1,
            name = "English",
            originalName = "English",
            tag = "en",
            flagEmoji = "🇬🇧",
            isBase = true
        ),
        TolgeeLanguageModel(
            id = 2,
            name = "Czech",
            originalName = "Čeština",
            tag = "cs",
            flagEmoji = "🇨🇿",
            isBase = false
        )
    )

    @Before
    fun setup() {
        remoteSource = mockk()
        languagePreferences = mockk()

        // Mock language preferences
        coEvery { languagePreferences.selectedLanguage } returns MutableStateFlow("en")
        coEvery { languagePreferences.saveSelectedLanguage(any()) } returns Unit

        val localSource = LanguageLocalSourceImpl(db.languageDao(), mapper)
        repository = LanguageRepository(remoteSource, localSource, languagePreferences)
    }

    @Test
    fun `findLanguages returns local data when available`() = runTest {
        // Given
        val languageDao = db.languageDao()
        languageDao.insertLanguages(testLanguages.map { mapper.mapLanguageModelToEntity(it) })

        // When
        val result = repository.findLanguages()

        // Then
        assertTrue(result is ResultWrapper.Success)
        assertEquals(testLanguages, (result as ResultWrapper.Success).data)
    }

    @Test
    fun `findLanguages fetches from remote when local is empty`() = runTest {
        // Given
        val remoteResponse = TolgeeLanguagesResponse(
            embedded = EmbeddedLanguages(
                languages = listOf(
                    TolgeeLanguageDto(
                        id = 1,
                        name = "English",
                        originalName = "English",
                        tag = "en",
                        flagEmoji = "🇬🇧",
                        isBase = true
                    ),
                    TolgeeLanguageDto(
                        id = 2,
                        name = "Czech",
                        originalName = "Čeština",
                        tag = "cs",
                        flagEmoji = "🇨🇿",
                        isBase = false
                    )
                )
            )
        )
        coEvery { remoteSource.getLanguages() } returns remoteResponse

        // When
        val result = repository.findLanguages()

        // Then
        assertTrue(result is ResultWrapper.Success)
        assertEquals(testLanguages, (result as ResultWrapper.Success).data)

        // Verify data was cached
        val localData = db.languageDao().getAllLanguages().first()
        assertEquals(testLanguages, localData.map { mapper.mapLanguageEntityToModel(it) })
    }

    @Test
    fun `clearLanguagesCache clears all languages`() = runTest {
        // Given
        val languageDao = db.languageDao()
        languageDao.insertLanguages(testLanguages.map { mapper.mapLanguageModelToEntity(it) })

        // When
        repository.clearLanguagesCache()

        // Then
        val localData = languageDao.getAllLanguages().first()
        assertTrue(localData.isEmpty())
    }

    @Test
    fun `getLanguages throws exception on remote error`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        coEvery { remoteSource.getLanguages() } throws exception

        try {
            // When
            repository.getLanguages()
        } catch (e: Exception) {
            // Then
            assertEquals(exception, e)
        }
    }

    @Test
    fun `initializeLanguage selects system language when not already set`() = runTest {
        // Given
        val languageDao = db.languageDao()
        languageDao.insertLanguages(testLanguages.map { mapper.mapLanguageModelToEntity(it) })

        coEvery { languagePreferences.selectedLanguage } returns MutableStateFlow("en")

        // When
        val result = repository.initializeLanguage(testLanguages)

        // Then
        assertTrue(result is ResultWrapper.Success)
        assertEquals("en", (result as ResultWrapper.Success).data)
    }
} 