package cz.fit.cvut.feature.init.data

import cz.fit.cvut.core.common.utils.InitStateManager
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.init.data.datastore.PreloadedTranslationsPreferences
import cz.fit.cvut.feature.init.data.source.PreloadedTranslationsDataSource
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.data.api.dto.response.EmbeddedLanguages
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguageDto
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferences
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import cz.fit.cvut.feature.language.data.source.LanguageLocalSourceImpl
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.data.api.dto.response.*
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSourceImpl
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class InitRepositoryIntegrationTest : TestTolgeeDB() {
    private lateinit var repository: InitRepositoryImpl
    private lateinit var languageRemoteSource: LanguageRemoteSource
    private lateinit var translationsRemoteSource: TranslationsRemoteSource
    private lateinit var preloadedTranslationsDataSource: PreloadedTranslationsDataSource
    private lateinit var translationsLocalSource: TranslationsLocalSourceImpl
    private lateinit var languagePreferences: LanguagePreferences
    private lateinit var initStateManager: InitStateManager
    private lateinit var preloadedTranslationsPreferences: PreloadedTranslationsPreferences
    private val mapper = TolgeeEntityMapper()
    private val langMapper = LanguageEntityMapper()

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

    // Test SQL statements for preloaded data
    private val testSqlStatements = listOf(
        "INSERT OR REPLACE INTO tolgee_languages (languageId, languageName, originalName, languageTag, flagEmoji, isBase) VALUES (1, 'English', 'English', 'en', '🇬🇧', 1);",
        "INSERT OR REPLACE INTO tolgee_languages (languageId, languageName, originalName, languageTag, flagEmoji, isBase) VALUES (2, 'Czech', 'Čeština', 'cs', '🇨🇿', 0);",
        "INSERT OR REPLACE INTO tolgee_keys (keyId, keyName) VALUES (1, 'welcome');",
        "INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (1, 1, 1, 'Welcome');",
        "INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (2, 1, 2, 'Vítejte');"
    )

    @Before
    fun setup() {
        languageRemoteSource = mockk()
        translationsRemoteSource = mockk()
        preloadedTranslationsDataSource = mockk()
        languagePreferences = mockk()
        initStateManager = InitStateManager()
        preloadedTranslationsPreferences = mockk()
        
        // Mock language preferences
        coEvery { languagePreferences.selectedLanguage } returns MutableStateFlow("en")
        coEvery { languagePreferences.saveSelectedLanguage(any()) } returns Unit

        // Mock preloaded translations preferences
        coEvery { preloadedTranslationsPreferences.wasPreloaded } returns MutableStateFlow(false)
        coEvery { preloadedTranslationsPreferences.savePreloadedState(any()) } returns Unit

        val languageLocalSource = LanguageLocalSourceImpl(db.languageDao(), langMapper)
        val languageRepository = LanguageRepository(languageRemoteSource, languageLocalSource, languagePreferences)

        translationsLocalSource = TranslationsLocalSourceImpl(db.tolgeeDao(), mapper)
        val translationsRepository = TranslationsRepository(translationsRemoteSource, translationsLocalSource, languageRepository)

        repository = InitRepositoryImpl(
            languageRepository = languageRepository, 
            translationsRepository = translationsRepository, 
            preloadedTranslationsDataSource = preloadedTranslationsDataSource, 
            database = db, 
            initStateManager = initStateManager,
            preloadedTranslationsPreferences = preloadedTranslationsPreferences
        )
    }

    @Test
    fun `initFetching uses preloaded data when available and not previously preloaded`() = runTest {
        // Given - preloaded data is available and not previously preloaded
        every { preloadedTranslationsDataSource.isAvailable() } returns true
        coEvery { preloadedTranslationsDataSource.getSqlStatements() } returns ResultWrapper.Success(testSqlStatements)
        coEvery { preloadedTranslationsPreferences.wasPreloaded } returns MutableStateFlow(false)

        // Pre-populate database with some data that should be cleared
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(
            TolgeeKeyModel(
                keyId = 999,
                keyName = "old.key",
                translations = mapOf(
                    "en" to TolgeeTranslationModel(1L,"Old", testLanguages[0]),
                    "cs" to TolgeeTranslationModel(2L, "Starý", testLanguages[1])
                )
            )
        )
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Success)

        // Verify preloaded state was saved
        coVerify { preloadedTranslationsPreferences.savePreloadedState(true) }

        // Verify old data was replaced with preloaded data
        val savedLanguages = db.tolgeeDao().getAllLanguages().first()
        assertEquals(2, savedLanguages.size)
        assertTrue(savedLanguages.any { it.tag == "en" })
        assertTrue(savedLanguages.any { it.tag == "cs" })

        val savedKeys = db.tolgeeDao().getAllKeys().first()
        assertEquals(1, savedKeys.size)
        assertTrue(savedKeys.any { it.keyName == "welcome" })
        assertFalse(savedKeys.any { it.keyName == "old.key" })

        val keysList = translationsLocalSource.getAllKeys().first()

        val allTranslations = keysList.flatMap { key -> key.translations.values }
        assertEquals(2, allTranslations.size)

        // Verify remote sources were not called
        coVerify(exactly = 0) { languageRemoteSource.getLanguages() }
        coVerify(exactly = 0) { translationsRemoteSource.getTranslations(any(), any()) }
    }
    
    @Test
    fun `initFetching updates from network when already preloaded`() = runTest {
        // Given - translations were already preloaded
        coEvery { preloadedTranslationsPreferences.wasPreloaded } returns MutableStateFlow(true)
        
        // Given - prepare mock responses for network
        coEvery { languageRemoteSource.getLanguages() } returns TolgeeLanguagesResponse(
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

        coEvery { translationsRemoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "test.key",
                        translations = mapOf(
                            "en" to Translation(1, "Hello"),
                            "cs" to Translation(2, "Ahoj")
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Success)
        
        // Verify network was called
        coVerify(exactly = 1) { languageRemoteSource.getLanguages() }
        coVerify(exactly = 1) { translationsRemoteSource.getTranslations(any(), any()) }
        
        // Verify preloaded data was not accessed
        coVerify(exactly = 0) { preloadedTranslationsDataSource.isAvailable() }
        coVerify(exactly = 0) { preloadedTranslationsDataSource.getSqlStatements() }
        
        // Verify data was saved
        val savedLanguages = db.tolgeeDao().getAllLanguages().first()
        assertTrue(savedLanguages.any { it.tag == "en" })
        assertTrue(savedLanguages.any { it.tag == "cs" })

        val savedKeys = db.tolgeeDao().getAllKeys().first()
        assertTrue(savedKeys.any { it.keyName == "test.key" })
    }

    @Test
    fun `initFetching falls back to network when preloaded data is not available`() = runTest {
        // Given - preloaded data is not available
        every { preloadedTranslationsDataSource.isAvailable() } returns false

        // Given - prepare mock responses for network fallback
        coEvery { languageRemoteSource.getLanguages() } returns TolgeeLanguagesResponse(
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

        coEvery { translationsRemoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "test.key",
                        translations = mapOf(
                            "en" to Translation(1, "Hello"),
                            "cs" to Translation(2, "Ahoj")
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Success)

        // Verify data was fetched from network
        coVerify(exactly = 1) { languageRemoteSource.getLanguages() }
        coVerify(exactly = 1) { translationsRemoteSource.getTranslations(any(), any()) }

        // Verify preloaded data was not used
        coVerify(exactly = 0) { preloadedTranslationsDataSource.getSqlStatements() }

        // Verify data was saved
        val savedLanguages = db.tolgeeDao().getAllLanguages().first()
        assertTrue(savedLanguages.any { it.tag == "en" })
        assertTrue(savedLanguages.any { it.tag == "cs" })

        val savedKeys = db.tolgeeDao().getAllKeys().first()
        assertTrue(savedKeys.any { it.keyName == "test.key" })
    }

    @Test
    fun `initFetching falls back to network when preloaded data fails`() = runTest {
        // Given - preloaded data is available but fails
        every { preloadedTranslationsDataSource.isAvailable() } returns true
        coEvery { preloadedTranslationsDataSource.getSqlStatements() } returns ResultWrapper.Error("Failed to load SQL")

        // Given - prepare mock responses for network fallback
        coEvery { languageRemoteSource.getLanguages() } returns TolgeeLanguagesResponse(
            embedded = EmbeddedLanguages(
                languages = listOf(
                    TolgeeLanguageDto(
                        id = 1,
                        name = "English",
                        originalName = "English",
                        tag = "en",
                        flagEmoji = "🇬🇧",
                        isBase = true
                    )
                )
            )
        )

        coEvery { translationsRemoteSource.getTranslations(0, "en") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "fallback.key",
                        translations = mapOf(
                            "en" to Translation(1, "Fallback")
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Success)

        // Verify network fallback was used
        coVerify(exactly = 1) { languageRemoteSource.getLanguages() }
        coVerify(exactly = 1) { translationsRemoteSource.getTranslations(any(), any()) }

        // Verify data was saved from network
        val savedKeys = db.tolgeeDao().getAllKeys().first()
        assertTrue(savedKeys.any { it.keyName == "fallback.key" })
    }

    @Test
    fun `initFetching returns error when language fetch fails and no preloaded data`() = runTest {
        // Given - preloaded data is not available
        every { preloadedTranslationsDataSource.isAvailable() } returns false

        // Given - network fails
        coEvery { languageRemoteSource.getLanguages() } throws Exception("Network error")

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Error)
        assertTrue((result as ResultWrapper.Error).message.contains("Failed to fetch languages"))
    }

    @Test
    fun `initFetching returns error when translations fetch fails and no preloaded data`() = runTest {
        // Given - preloaded data is not available
        every { preloadedTranslationsDataSource.isAvailable() } returns false

        coEvery { languageRemoteSource.getLanguages() } returns TolgeeLanguagesResponse(
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

        // Given - translations fetch fails
        coEvery { translationsRemoteSource.getTranslations(any(), any()) } throws Exception("Network error")

        // When
        val result = repository.initFetching()

        // Then
        assertTrue(result is ResultWrapper.Error)
        assertTrue((result as ResultWrapper.Error).message.contains("Failed to fetch translations"))
    }
}