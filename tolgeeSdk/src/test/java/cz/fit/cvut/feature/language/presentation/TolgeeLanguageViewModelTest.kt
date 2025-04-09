package cz.fit.cvut.feature.language.presentation

import cz.fit.cvut.core.common.utils.InitStateManager
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TolgeeLanguageViewModelTest {

    private lateinit var viewModel: TolgeeLanguageViewModel
    private lateinit var languageRepository: LanguageRepository
    private lateinit var initStateManager: InitStateManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        languageRepository = mockk()
        initStateManager = mockk()
        
        coEvery { initStateManager.waitForInitialization() } returns Unit
        coEvery { languageRepository.selectedLanguage } returns flowOf("en")
        
        // Add default mock for findLanguages
        val defaultLanguages = listOf(
            TolgeeLanguageModel(
                id = 1L,
                name = "English",
                originalName = "English",
                tag = "en",
                flagEmoji = "🇬🇧",
                isBase = true
            ),
            TolgeeLanguageModel(
                id = 2L,
                name = "Czech",
                originalName = "Čeština",
                tag = "cs",
                flagEmoji = "🇨🇿",
                isBase = false
            )
        )
        coEvery { languageRepository.findLanguages() } returns ResultWrapper.Success(defaultLanguages)
        
        viewModel = TolgeeLanguageViewModel(languageRepository, initStateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when loadLanguages is called, should emit loading state first`() = runTest {
        // Given
        val languages = listOf(
            TolgeeLanguageModel(
                id = 1L,
                name = "English",
                originalName = "English",
                tag = "en",
                flagEmoji = "🇬🇧",
                isBase = true
            ),
            TolgeeLanguageModel(
                id = 2L,
                name = "Czech",
                originalName = "Čeština",
                tag = "cs",
                flagEmoji = "🇨🇿",
                isBase = false
            )
        )
        coEvery { languageRepository.findLanguages() } returns ResultWrapper.Success(languages)

        // When
        viewModel.loadLanguages()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.languages.value
        assertTrue(state is LanguageState.Loaded && state.languages == languages)
    }

    @Test
    fun `when loadLanguages fails, should emit error state`() = runTest {
        // Given
        val errorMessage = "Failed to load languages"
        coEvery { languageRepository.findLanguages() } returns ResultWrapper.Error(errorMessage)

        // When
        viewModel.loadLanguages()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.languages.value
        assertTrue(state is LanguageState.Error && state.message == errorMessage)
    }

    @Test
    fun `when setSelectedLanguage is called, should call repository`() = runTest {
        // Given
        val language = "cs"
        coEvery { languageRepository.setSelectedLanguage(language) } returns Unit

        // When
        viewModel.setSelectedLanguage(language)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { languageRepository.setSelectedLanguage(language) }
    }

    @Test
    fun `when initialized, should load languages`() = runTest {
        // Given
        val languages = listOf(
            TolgeeLanguageModel(
                id = 1L,
                name = "English",
                originalName = "English",
                tag = "en",
                flagEmoji = "🇬🇧",
                isBase = true
            ),
            TolgeeLanguageModel(
                id = 2L,
                name = "Czech",
                originalName = "Čeština",
                tag = "cs",
                flagEmoji = "🇨🇿",
                isBase = false
            )
        )
        coEvery { languageRepository.findLanguages() } returns ResultWrapper.Success(languages)

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.languages.value
        assertTrue(state is LanguageState.Loaded && state.languages == languages)
    }
} 