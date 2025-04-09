package cz.fit.cvut.feature.translation.data.source

import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import java.util.UUID
import kotlin.random.Random.Default.nextLong
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TranslationsLocalSourceStressTest : TestTolgeeDB() {
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
        ),
        TolgeeLanguageModel(
            id = 3,
            name = "German",
            originalName = "Deutsch",
            tag = "de",
            flagEmoji = "🇩🇪",
            isBase = false
        ),
        TolgeeLanguageModel(
            id = 4,
            name = "Spanish",
            originalName = "Español",
            tag = "es",
            flagEmoji = "🇪🇸",
            isBase = false
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

    private fun createTestKey(id: Long, suffix: String) = TolgeeKeyModel(
        keyId = id,
        keyName = "test.key.$suffix",
        translations = testLanguages.associate { lang ->
            lang.tag to TolgeeTranslationModel(
                text = "Translation $suffix for ${lang.name}",
                language = lang,
                id = nextLong()
            )
        }
    )

    @Test
    fun `stress test - batch insert large number of keys`() = runTest {
        // Given - prepare 10000 keys with translations for 4 languages each
        val largeKeySet = (1..10000L).map { id ->
            createTestKey(id, "batch_$id")
        }

        // When - measure time to insert all keys
        val insertTime = measureTimeMillis {
            translationsLocalSource.saveKeys(largeKeySet)
        }
        println("Time to insert 10000 keys with translations: $insertTime ms")

        // Then - verify all data was saved correctly
        val savedKeys = translationsLocalSource.getAllKeys().first()
        assertEquals(10000, savedKeys.size)
        
        // Verify random samples
        val samples = savedKeys.shuffled().take(10)
        samples.forEach { key ->
            assertEquals(4, key.translations.size)
            testLanguages.forEach { lang ->
                assertNotNull(key.translations[lang.tag])
            }
        }
    }

    @Test
    fun `stress test - concurrent reads and writes`() = runTest {
        // Given - prepare initial dataset
        val initialKeys = (1..1000L).map { id ->
            createTestKey(id, "concurrent_$id")
        }
        translationsLocalSource.saveKeys(initialKeys)

        // When - perform concurrent operations
        val jobs = List(100) { jobId ->
            launch {
                when (jobId % 3) {
                    0 -> {
                        // Read operation
                        val keys = translationsLocalSource.getAllKeys().first()
                        assertTrue(keys.isNotEmpty())
                    }
                    1 -> {
                        // Write operation - update existing key
                        val keyToUpdate = createTestKey(
                            id = (jobId % 1000 + 1).toLong(),
                            suffix = "updated_$jobId"
                        )
                        translationsLocalSource.saveKey(keyToUpdate)
                    }
                    2 -> {
                        // Write operation - add new key
                        val newKey = createTestKey(
                            id = 10000L + jobId,
                            suffix = "new_$jobId"
                        )
                        translationsLocalSource.saveKey(newKey)
                    }
                }
            }
        }

        // Wait for all operations to complete
        jobs.forEach { it.join() }

        // Then - verify database consistency
        val finalKeys = translationsLocalSource.getAllKeys().first()
        assertTrue(finalKeys.size >= 1000)
        finalKeys.forEach { key ->
            assertEquals(4, key.translations.size)
        }
    }

    @Test
    fun `stress test - rapid updates to same key`() = runTest {
        // Given
        val testKey = createTestKey(1L, "rapid_update")
        translationsLocalSource.saveKey(testKey)

        // When - perform 1000 rapid updates to the same key
        val updateTime = measureTimeMillis {
            repeat(1000) { iteration ->
                val updatedKey = testKey.copy(
                    translations = testKey.translations.mapValues { (lang, translation) ->
                        translation.copy(
                            text = "Update $iteration for $lang"
                        )
                    }
                )
                translationsLocalSource.saveKey(updatedKey)
            }
        }
        println("Time for 1000 updates to same key: $updateTime ms")

        // Then - verify final state
        val savedKey = translationsLocalSource.observeKeyByName(testKey.keyName).first()
        assertNotNull(savedKey)
        assertEquals(4, savedKey?.translations?.size)
        savedKey?.translations?.values?.forEach { translation ->
            translation.text?.let { assertTrue(it.startsWith("Update 999")) }
        }
    }

    @Test
    fun `stress test - memory usage during large operation`() = runTest {
        // Given - prepare large dataset with long texts
        val largeTexts = List(1000) { index ->
            "Very long translation text that is repeated multiple times to consume memory. ".repeat(100) +
            "This is variation $index to make each text unique."
        }

        val largeKeys = (1..1000L).map { id ->
            TolgeeKeyModel(
                keyId = id,
                keyName = "memory.test.key.$id",
                translations = testLanguages.associate { lang ->
                    lang.tag to TolgeeTranslationModel(
                        text = largeTexts[(id % 1000).toInt()],
                        language = lang,
                        id = id
                    )
                }
            )
        }

        // When - perform operations with large dataset
        translationsLocalSource.saveKeys(largeKeys)
        
        // Then - verify we can still read and process the data
        val savedKeys = translationsLocalSource.getAllKeys().first()
        assertEquals(1000, savedKeys.size)
        
        // Perform some heavy operations
        val totalLength = savedKeys.flatMap { key ->
            key.translations.values.mapNotNull { translation ->
                translation.text?.length
            }
        }.sum()
        
        assertTrue(totalLength > 1_000_000) // Verify we processed significant amount of data
    }

    @Test
    fun `stress test - rapid deletion and insertion cycles`() = runTest {
        repeat(100) { cycle ->
            // Delete all existing data
            translationsLocalSource.deleteAllKeys()
            
            // Verify deletion
            val keysAfterDelete = translationsLocalSource.getAllKeys().first()
            assertTrue(keysAfterDelete.isEmpty())
            
            // Insert new batch of data
            val newKeys = (1..100L).map { id ->
                createTestKey(id, "cycle_${cycle}_$id")
            }
            translationsLocalSource.saveKeys(newKeys)
            
            // Verify insertion
            val keysAfterInsert = translationsLocalSource.getAllKeys().first()
            assertEquals(100, keysAfterInsert.size)
        }
    }
} 