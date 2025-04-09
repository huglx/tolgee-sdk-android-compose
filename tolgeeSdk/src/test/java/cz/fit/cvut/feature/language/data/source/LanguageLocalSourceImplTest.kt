package cz.fit.cvut.feature.language.data.source

import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
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
class LanguageLocalSourceImplTest : TestTolgeeDB() {
    private lateinit var languageLocalSource: LanguageLocalSourceImpl
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
        languageLocalSource = LanguageLocalSourceImpl(db.languageDao(), mapper)
    }

    @Test
    fun `getAllLanguages returns empty list when database is empty`() = runTest {
        val languages = languageLocalSource.getAllLanguages().first()
        assertTrue(languages.isEmpty())
    }

    @Test
    fun `saveLanguages stores languages in database`() = runTest {
        // When
        languageLocalSource.saveLanguages(testLanguages)

        // Then
        val savedLanguages = languageLocalSource.getAllLanguages().first()
        assertEquals(2, savedLanguages.size)
        
        val englishLang = savedLanguages.find { it.tag == "en" }
        assertNotNull(englishLang)
        assertEquals("English", englishLang?.name)
        assertEquals("English", englishLang?.originalName)
        assertEquals("🇬🇧", englishLang?.flagEmoji)
        assertTrue(englishLang?.isBase == true)

        val czechLang = savedLanguages.find { it.tag == "cs" }
        assertNotNull(czechLang)
        assertEquals("Czech", czechLang?.name)
        assertEquals("Čeština", czechLang?.originalName)
        assertEquals("🇨🇿", czechLang?.flagEmoji)
        assertFalse(czechLang?.isBase == true)
    }

    @Test
    fun `saveLanguages updates existing languages`() = runTest {
        // Given
        languageLocalSource.saveLanguages(testLanguages)

        // When
        val updatedLanguages = testLanguages.map { lang ->
            when (lang.tag) {
                "en" -> lang.copy(name = "Updated English")
                "cs" -> lang.copy(name = "Updated Czech")
                else -> lang
            }
        }
        languageLocalSource.saveLanguages(updatedLanguages)

        // Then
        val savedLanguages = languageLocalSource.getAllLanguages().first()
        assertEquals(2, savedLanguages.size)
        
        val englishLang = savedLanguages.find { it.tag == "en" }
        assertEquals("Updated English", englishLang?.name)
        
        val czechLang = savedLanguages.find { it.tag == "cs" }
        assertEquals("Updated Czech", czechLang?.name)
    }

    @Test
    fun `deleteAllLanguages removes all languages from database`() = runTest {
        // Given
        languageLocalSource.saveLanguages(testLanguages)
        val initialLanguages = languageLocalSource.getAllLanguages().first()
        assertEquals(2, initialLanguages.size)

        // When
        languageLocalSource.deleteAllLanguages()

        // Then
        val languagesAfterDelete = languageLocalSource.getAllLanguages().first()
        assertTrue(languagesAfterDelete.isEmpty())
    }

    @Test
    fun `saveLanguages preserves language order`() = runTest {
        // Given
        val orderedLanguages = listOf(
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
            )
        )

        // When
        languageLocalSource.saveLanguages(orderedLanguages)

        // Then
        val savedLanguages = languageLocalSource.getAllLanguages().first()
        assertEquals(3, savedLanguages.size)
        assertEquals("en", savedLanguages[0].tag)
        assertEquals("cs", savedLanguages[1].tag)
        assertEquals("de", savedLanguages[2].tag)
    }
} 