package cz.fit.cvut.feature.translation.presentation.list

import app.cash.turbine.test
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.data.api.dto.response.*
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSourceImpl
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.translation.domain.usecases.GetTranslationsForAvailableLanguagesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TranslationsListViewModelTest : TestTolgeeDB() {
    private lateinit var viewModel: TranslationsListViewModel
    private lateinit var translationsRemoteSource: TranslationsRemoteSource
    private lateinit var languageRemoteSource: LanguageRemoteSource
    private lateinit var getTranslationsForAvailableLanguagesUseCase: GetTranslationsForAvailableLanguagesUseCase
    private val mapper = TolgeeEntityMapper()
    private val testDispatcher = StandardTestDispatcher()

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

    private val testTranslations = listOf(
        TolgeeKeyModel(
            keyId = 1,
            keyName = "test.key1",
            translations = mapOf(
                "en" to TolgeeTranslationModel(1L,"Hello", testLanguages[0]),
                "cs" to TolgeeTranslationModel(2L,"Ahoj", testLanguages[1])
            )
        ),
        TolgeeKeyModel(
            keyId = 2,
            keyName = "test.key2",
            translations = mapOf(
                "en" to TolgeeTranslationModel(1L,"World", testLanguages[0]),
                "cs" to TolgeeTranslationModel(2L,"Světe", testLanguages[1])
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        translationsRemoteSource = mockk()
        languageRemoteSource = mockk()

        // Setup real repositories with in-memory database
        val translationsLocalSource = TranslationsLocalSourceImpl(db.tolgeeDao(), mapper)
        val languageRepository = mockk<LanguageRepository>()
        val translationsRepository = TranslationsRepository(translationsRemoteSource, translationsLocalSource, languageRepository)

        // Mock language repository to return test languages
        coEvery { languageRepository.findLanguages() } returns ResultWrapper.Success(testLanguages)

        // Create real use case with mocked dependencies
        getTranslationsForAvailableLanguagesUseCase = GetTranslationsForAvailableLanguagesUseCase(
            translationsRepository,
            languageRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        coEvery { translationsRemoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = emptyList(),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        viewModel = TranslationsListViewModel(
            translationsRepository = TranslationsRepository(translationsRemoteSource, TranslationsLocalSourceImpl(db.tolgeeDao(), mapper), mockk()),
            getTranslationsForAvailableLanguagesUseCase = getTranslationsForAvailableLanguagesUseCase
        )

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(TranslationsListViewModel.TranslationListState.IsLoading, awaitItem())
            assertTrue(awaitItem() is TranslationsListViewModel.TranslationListState.Loaded)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loads translations successfully from remote`() = runTest {
        // Given
        coEvery { translationsRemoteSource.getTranslations(0, "en,cs") } returns TolgeeKeyResponse(
            embedded = EmbeddedKeys(
                keys = listOf(
                    Key(
                        keyId = 1,
                        keyName = "test.key1",
                        translations = mapOf(
                            "en" to Translation(1, "Hello"),
                            "cs" to Translation(2, "Ahoj")
                        )
                    ),
                    Key(
                        keyId = 2,
                        keyName = "test.key2",
                        translations = mapOf(
                            "en" to Translation(3, "World"),
                            "cs" to Translation(4, "Světe")
                        )
                    )
                ),
                totalPages = 1,
                currentPage = 0
            )
        )

        // When
        viewModel = TranslationsListViewModel(
            translationsRepository = TranslationsRepository(translationsRemoteSource, TranslationsLocalSourceImpl(db.tolgeeDao(), mapper), mockk()),
            getTranslationsForAvailableLanguagesUseCase = getTranslationsForAvailableLanguagesUseCase
        )

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(TranslationsListViewModel.TranslationListState.IsLoading, awaitItem())
            val loadedState = awaitItem() as TranslationsListViewModel.TranslationListState.Loaded
            assertEquals(2, loadedState.translations.size)
            assertEquals("test.key1", loadedState.translations[0].keyName)
            assertEquals("test.key2", loadedState.translations[1].keyName)
            assertEquals("Hello", loadedState.translations[0].translations["en"]?.text)
            assertEquals("Světe", loadedState.translations[1].translations["cs"]?.text)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits error state when loading fails`() = runTest {
        // Given
        coEvery { translationsRemoteSource.getTranslations(any(), any()) } throws Exception("Network error")

        // When
        viewModel = TranslationsListViewModel(
            translationsRepository = TranslationsRepository(translationsRemoteSource, TranslationsLocalSourceImpl(db.tolgeeDao(), mapper), mockk()),
            getTranslationsForAvailableLanguagesUseCase = getTranslationsForAvailableLanguagesUseCase
        )

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(TranslationsListViewModel.TranslationListState.IsLoading, awaitItem())
            val errorState = awaitItem() as TranslationsListViewModel.TranslationListState.Error
            assertTrue(errorState.message.contains("Network error"))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `returns cached translations when available`() = runTest {
        // Given - save translations to database first
        testTranslations.forEach { translation ->
            val (keyEntity, languageEntities, translationEntities) = mapper.toEntity(translation)
            db.tolgeeDao().insertKeys(listOf(keyEntity))
            db.tolgeeDao().insertLanguages(languageEntities)
            db.tolgeeDao().insertTranslations(translationEntities)
        }

        // Mock network error to ensure data is retrieved from cache
        coEvery { translationsRemoteSource.getTranslations(any(), any()) } throws Exception("Network error")

        // When
        viewModel = TranslationsListViewModel(
            translationsRepository = TranslationsRepository(translationsRemoteSource, TranslationsLocalSourceImpl(db.tolgeeDao(), mapper), mockk()),
            getTranslationsForAvailableLanguagesUseCase = getTranslationsForAvailableLanguagesUseCase
        )

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(TranslationsListViewModel.TranslationListState.IsLoading, awaitItem())
            val loadedState = awaitItem() as TranslationsListViewModel.TranslationListState.Loaded
            coVerify(exactly = 0) { translationsRemoteSource.getTranslations(any(), any()) }
            assertEquals(2, loadedState.translations.size)
            assertEquals("test.key1", loadedState.translations[0].keyName)
            assertEquals("test.key2", loadedState.translations[1].keyName)
            cancelAndConsumeRemainingEvents()
        }
    }
} 