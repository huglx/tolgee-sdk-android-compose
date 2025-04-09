package cz.fit.cvut.feature.translation.data.source

import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TranslationsLocalSourceImplTest : TestTolgeeDB() {
    private lateinit var translationsLocalSource: TranslationsLocalSourceImpl
    private val mapper = TolgeeEntityMapper()
    private val languageMapper = LanguageEntityMapper()

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
            "cs" to TolgeeTranslationModel(2L,"Ahoj", testLanguages[1])
        )
    )

    private val testKeys = listOf(
        testKey,
        TolgeeKeyModel(
            keyId = 2,
            keyName = "test.key2",
            translations = mapOf(
                "en" to TolgeeTranslationModel(3L,"World", testLanguages[0]),
                "cs" to TolgeeTranslationModel(4L,"Světe", testLanguages[1])
            )
        )
    )

    @Before
    fun setup() {
        translationsLocalSource = TranslationsLocalSourceImpl(db.tolgeeDao(), mapper)
        
        // Insert test languages into database
        runTest {
            val languageEntities = testLanguages.map { languageMapper.mapLanguageModelToEntity(it) }
            db.languageDao().insertLanguages(languageEntities)
        }
    }

    @Test
    fun `getAllKeys returns empty list when database is empty`() = runTest {
        val keys = translationsLocalSource.getAllKeys().first()
        assertTrue(keys.isEmpty())
    }

    @Test
    fun `saveKey stores key with translations in database`() = runTest {
        // When
        translationsLocalSource.saveKey(testKey)

        // Then
        val savedKeys = translationsLocalSource.getAllKeys().first()
        assertEquals(1, savedKeys.size)
        
        val savedKey = savedKeys[0]
        assertEquals(testKey.keyId, savedKey.keyId)
        assertEquals(testKey.keyName, savedKey.keyName)
        assertEquals(testKey.translations["en"]?.text, savedKey.translations["en"]?.text)
        assertEquals(testKey.translations["cs"]?.text, savedKey.translations["cs"]?.text)
    }

    @Test
    fun `saveKeys stores multiple keys with translations in database`() = runTest {
        // When
        translationsLocalSource.saveKeys(testKeys)

        // Then
        val savedKeys = translationsLocalSource.getAllKeys().first()
        assertEquals(2, savedKeys.size)
        
        val firstKey = savedKeys.find { it.keyName == "test.key" }
        assertNotNull(firstKey)
        assertEquals("Hello", firstKey?.translations?.get("en")?.text)
        assertEquals("Ahoj", firstKey?.translations?.get("cs")?.text)

        val secondKey = savedKeys.find { it.keyName == "test.key2" }
        assertNotNull(secondKey)
        assertEquals("World", secondKey?.translations?.get("en")?.text)
        assertEquals("Světe", secondKey?.translations?.get("cs")?.text)
    }

    @Test
    fun `observeKeyByName returns null when key does not exist`() = runTest {
        val key = translationsLocalSource.observeKeyByName("non.existent.key").first()
        assertNull(key)
    }

    @Test
    fun `observeKeyByName returns key when it exists`() = runTest {
        // Given
        translationsLocalSource.saveKey(testKey)

        // When
        val savedKey = translationsLocalSource.observeKeyByName(testKey.keyName).first()

        // Then
        assertNotNull(savedKey)
        assertEquals(testKey.keyId, savedKey?.keyId)
        assertEquals(testKey.keyName, savedKey?.keyName)
        assertEquals(testKey.translations["en"]?.text, savedKey?.translations?.get("en")?.text)
        assertEquals(testKey.translations["cs"]?.text, savedKey?.translations?.get("cs")?.text)
    }

    @Test
    fun `deleteAllKeys removes all keys and translations from database`() = runTest {
        // Given
        translationsLocalSource.saveKeys(testKeys)
        val initialKeys = translationsLocalSource.getAllKeys().first()
        assertEquals(2, initialKeys.size)

        // When
        translationsLocalSource.deleteAllKeys()

        // Then
        val keysAfterDelete = translationsLocalSource.getAllKeys().first()
        assertTrue(keysAfterDelete.isEmpty())
    }

    @Test
    fun `saveKey updates existing key with new translations`() = runTest {
        // Given
        translationsLocalSource.saveKey(testKey)

        // When
        val updatedKey = testKey.copy(
            translations = mapOf(
                "en" to TolgeeTranslationModel(1L,"Updated Hello", testLanguages[0]),
                "cs" to TolgeeTranslationModel(2L,"Aktualizované Ahoj", testLanguages[1])
            )
        )
        translationsLocalSource.saveKey(updatedKey)

        // Then
        val savedKey = translationsLocalSource.observeKeyByName(testKey.keyName).first()
        assertNotNull(savedKey)
        assertEquals("Updated Hello", savedKey?.translations?.get("en")?.text)
        assertEquals("Aktualizované Ahoj", savedKey?.translations?.get("cs")?.text)
    }
} 