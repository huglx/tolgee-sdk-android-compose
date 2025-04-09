package cz.fit.cvut.feature.translation.data

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.*
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSourceImpl
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class TranslationsRepositoryIntegrationTest : TestTolgeeDB() {
    private lateinit var repository: TranslationsRepository
    private lateinit var langRepository: LanguageRepository
    private lateinit var remoteSource: TranslationsRemoteSource
    private val mapper = TolgeeEntityMapper()

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

    private val testKey = TolgeeKeyModel(
        keyId = 1,
        keyName = "test.key",
        translations = mapOf(
            "en" to TolgeeTranslationModel(1L,"Hello", testLanguages[0]),
            "cs" to TolgeeTranslationModel(2L, "Ahoj", testLanguages[1])
        )
    )

    @Before
    fun setup() {
        remoteSource = mockk()
        langRepository = mockk()
        val localSource = TranslationsLocalSourceImpl(db.tolgeeDao(), mapper)
        repository = TranslationsRepository(remoteSource, localSource, langRepository)
    }

    @Test
    fun `getTranslations returns local data when available`() = runTest {
        // Given
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // When
        val result = repository.getTranslations(testLanguages)

        // Then
        assertTrue(result is ResultWrapper.Success)
        assertEquals(listOf(testKey), (result as ResultWrapper.Success).data)
        coVerify(exactly = 0) { remoteSource.getTranslations(any(), any()) }
    }

    @Test
    fun `getTranslations fetches from remote when local is empty`() = runTest {
        // Given
        coEvery { remoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
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
        val result = repository.getTranslations(testLanguages)

        // Then
        assertTrue(result is ResultWrapper.Success)
        coVerify { remoteSource.getTranslations(0, "en,cs") }
        
        // Verify data was cached in database
        val localData = db.tolgeeDao().getAllKeys().first()
        assertEquals(1, localData.size)
        assertEquals("test.key", localData[0].keyName)
    }

    @Test
    fun `observeTranslationForKey returns Flow from local source`() = runTest {
        // Given
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // When & Then
        turbineScope {
            repository.observeTranslationForKey("test.key")
                .test(timeout = 5.seconds) {
                    val result = awaitItem()
                    assertEquals(testKey.keyName, result?.keyName)
                    assertEquals(testKey.translations["en"]?.text, result?.translations?.get("en")?.text)
                    assertEquals(testKey.translations["cs"]?.text, result?.translations?.get("cs")?.text)
                    cancelAndConsumeRemainingEvents()
                }
        }
    }

    @Test
    fun `updateTranslationWithContext updates local cache on success`() = runTest {
        // Given
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        val request = UpdateTranslationContextRequest(
            name = "test.key",
            relatedKeysInOrder = emptyList(),
            translations = mapOf("en" to "Updated Hello", "cs" to "Aktualizované Ahoj")
        )
        
        val response = UpdateTranslationsContextResponse(
            description = null,
            keyId = 1,
            keyName = "test.key",
            translations = mapOf(
                "en" to TranslationResponse(1, "Updated Hello"),
                "cs" to TranslationResponse(2, "Aktualizované Ahoj")
            )
        )

        coEvery { remoteSource.updateTranslationWithContext(request, 1) } returns response

        // When
        val result = repository.updateTranslationWithContext(request, 1)

        // Then
        assertTrue(result is ResultWrapper.Success)
        
        // Verify database was updated
        turbineScope {
            repository.observeTranslationForKey("test.key")
                .test(timeout = 5.seconds) {
                    val updatedKey = awaitItem()
                    assertEquals("Updated Hello", updatedKey?.translations?.get("en")?.text)
                    assertEquals("Aktualizované Ahoj", updatedKey?.translations?.get("cs")?.text)
                    cancelAndConsumeRemainingEvents()
                }
        }
    }

    @Test
    fun `clearLocalCache clears all keys`() = runTest {
        // Given
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // When
        repository.clearLocalCache()

        // Then
        val localKeys = db.tolgeeDao().getAllKeys().first()
        assertTrue(localKeys.isEmpty())
    }

    @Test
    fun `mergeAndSaveKeys correctly updates existing translations without creating duplicates`() = runTest {
        // Given - first add initial translations
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // Get initial translation IDs
        val initialTranslations = db.tolgeeDao().getTranslationsForKey(1)
        val initialEnTranslationId = initialTranslations.find { it.languageId == 1L }?.id
        val initialCsTranslationId = initialTranslations.find { it.languageId == 2L }?.id

        // When - update existing translation with the same keyId and languageId
        val updatedKey = TolgeeKeyModel(
            keyId = 1,
            keyName = "test.key",
            translations = mapOf(
                "en" to TolgeeTranslationModel(1, "Updated Hello", testLanguages[0]),
                "cs" to TolgeeTranslationModel(2, "Aktualizovaný Ahoj", testLanguages[1])
            )
        )

        val (updatedKeyEntity, _, updatedTranslationEntities) = mapper.toEntity(updatedKey)
        db.tolgeeDao().insertKeys(listOf(updatedKeyEntity))
        db.tolgeeDao().insertTranslations(updatedTranslationEntities)

        // Then - verify that translation IDs remain the same but texts are updated
        val updatedTranslations = db.tolgeeDao().getTranslationsForKey(1)

        // Should still have only 2 translations (not 4)
        assertEquals(2, updatedTranslations.size)

        // Translation IDs should remain the same
        val enTranslation = updatedTranslations.find { it.languageId == 1L }
        val csTranslation = updatedTranslations.find { it.languageId == 2L }

        assertEquals(initialEnTranslationId, enTranslation?.id)
        assertEquals(initialCsTranslationId, csTranslation?.id)

        // But text should be updated
        assertEquals("Updated Hello", enTranslation?.text)
        assertEquals("Aktualizovaný Ahoj", csTranslation?.text)
    }

    @Test
    fun `mergeAndSaveKeys adds new translations while preserving existing ones`() = runTest {
        // Given - first add initial translation for English only
        val partialKey = TolgeeKeyModel(
            keyId = 1,
            keyName = "test.key",
            translations = mapOf(
                "en" to TolgeeTranslationModel(1L, "Hello", testLanguages[0])
            )
        )

        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(partialKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // Get initial translation ID
        val initialEnTranslationId = db.tolgeeDao().getTranslationsForKey(1).first().id

        // When - add Czech translation to existing key
        val completeKey = TolgeeKeyModel(
            keyId = 1,
            keyName = "test.key",
            translations = mapOf(
                "en" to TolgeeTranslationModel(0, "Hello", testLanguages[0]),
                "cs" to TolgeeTranslationModel(0, "Ahoj", testLanguages[1])
            )
        )

        val (updatedKeyEntity, updateLanguageEntities, updatedTranslationEntities) = mapper.toEntity(completeKey)
        db.tolgeeDao().insertKeys(listOf(updatedKeyEntity))
        db.tolgeeDao().insertLanguages(updateLanguageEntities)
        db.tolgeeDao().insertTranslations(updatedTranslationEntities)

        // Then - verify that a new translation was added while the existing one kept its ID
        val updatedTranslations = db.tolgeeDao().getTranslationsForKey(1)

        // Should now have 2 translations
        assertEquals(2, updatedTranslations.size)

        // English translation ID should remain the same
        val enTranslation = updatedTranslations.find { it.languageId == 1L }
        assertEquals(initialEnTranslationId, enTranslation?.id)
        assertEquals("Hello", enTranslation?.text)

        // Czech translation should be added
        val csTranslation = updatedTranslations.find { it.languageId == 2L }
        assertNotNull(csTranslation)
        assertEquals("Ahoj", csTranslation?.text)
    }

    @Test
    fun `getTranslations merges network data with existing translations`() = runTest {
        // Given - add translation to the database first
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // Get initial counts and IDs
        val initialCount = db.tolgeeDao().getTranslationsCount()
        val initialTranslations = db.tolgeeDao().getTranslationsForKey(1)
        val initialEnTranslationId = initialTranslations.find { it.languageId == 1L }?.id

        // Mock response with updated translation for English and new for French
        coEvery { remoteSource.getTranslations(0, "en,cs,fr") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "test.key",
                        translations = mapOf(
                            "en" to Translation(1, "Updated from network"),
                            "fr" to Translation(100, "Bonjour")              // New language
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // Add French to test languages
        val frenchLanguage = TolgeeLanguageModel(
            id = 3,
            name = "French",
            originalName = "Français",
            tag = "fr",
            flagEmoji = "🇫🇷",
            isBase = false
        )
        val testLanguagesWithFrench = testLanguages + frenchLanguage

        // When
        val result = repository.fetchAndCacheTranslations(testLanguagesWithFrench)

        // Then
        assertTrue(result is ResultWrapper.Success)

        val updatedTranslations = db.tolgeeDao().getTranslationsForKey(1)
        assertEquals(3, updatedTranslations.size)

        // English translation should be updated but keep the same ID
        val enTranslation = updatedTranslations.find { it.languageId == 1L }
        assertEquals(initialEnTranslationId, enTranslation?.id)
        assertEquals("Updated from network", enTranslation?.text)

        // Czech translation should remain unchanged
        val csTranslation = updatedTranslations.find { it.languageId == 2L }
        assertEquals("Ahoj", csTranslation?.text)

        // French translation should be added
        val frTranslation = updatedTranslations.find { it.languageId == 3L }
        assertNotNull(frTranslation)
        assertEquals("Bonjour", frTranslation?.text)

        // Total translation count should increase by 1, not 2
        // (because one was updated, and one was added)
        assertEquals(initialCount + 1, db.tolgeeDao().getTranslationsCount())
    }

    @Test
    fun `refreshTranslations updates existing translations without creating duplicates`() = runTest {
        // Given - add translation to the database first
        val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(testKey)
        db.tolgeeDao().insertKeys(listOf(keyEntity))
        db.tolgeeDao().insertLanguages(languageEntities)
        db.tolgeeDao().insertTranslations(translationEntities)

        // Get initial counts
        val initialCount = db.tolgeeDao().getTranslationsCount()

        // Mock response
        coEvery { remoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "test.key",
                        translations = mapOf(
                            "en" to Translation(1, "Refreshed text"),
                            "cs" to Translation(2, "Obnovený text")
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        val result = repository.refreshTranslations(testLanguages)

        // Then
        assertTrue(result is ResultWrapper.Success)

        // Translation count should remain the same
        assertEquals(initialCount, db.tolgeeDao().getTranslationsCount())

        // Texts should be updated
        val updatedTranslations = db.tolgeeDao().getTranslationsForKey(1)
        val enTranslation = updatedTranslations.find { it.languageId == 1L }
        val csTranslation = updatedTranslations.find { it.languageId == 2L }

        assertEquals("Refreshed text", enTranslation?.text)
        assertEquals("Obnovený text", csTranslation?.text)
    }
} 