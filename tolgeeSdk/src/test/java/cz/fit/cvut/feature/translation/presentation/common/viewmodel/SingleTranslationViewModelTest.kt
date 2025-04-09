package cz.fit.cvut.feature.translation.presentation.common.viewmodel

import app.cash.turbine.test
import cz.fit.cvut.core.common.utils.InitStateManager
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.translation.domain.usecases.ObserveTranslationsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SingleTranslationViewModelTest {
    private lateinit var viewModel: SingleTranslationViewModel
    private lateinit var observeTranslationsUseCase: ObserveTranslationsUseCase
    private val testDispatcher = StandardTestDispatcher()
    private val initStateManager = InitStateManager()

    private val testLanguage = TolgeeLanguageModel(
        id = 1,
        name = "English",
        originalName = "English",
        tag = "en",
        flagEmoji = "🇬🇧",
        isBase = true
    )

    private val testTranslation = TolgeeKeyModel(
        keyId = 1,
        keyName = "test.key",
        translations = mapOf(
            "en" to TolgeeTranslationModel(1L,"Hello", testLanguage)
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        observeTranslationsUseCase = mockk()
        initStateManager.setInitialized()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        // Given
        coEvery { observeTranslationsUseCase(any()) } returns flowOf()

        // When
        viewModel = SingleTranslationViewModel("test.key", observeTranslationsUseCase, initStateManager)

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(SingleTranslationState.IsLoading, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits Available state when translation is found`() = runTest {
        // Given
        coEvery { observeTranslationsUseCase(any()) } returns flowOf(
            SingleTranslationState.Available(testTranslation, "en")
        )

        // When
        viewModel = SingleTranslationViewModel("test.key", observeTranslationsUseCase, initStateManager)

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(SingleTranslationState.IsLoading, awaitItem())
            val availableState = awaitItem() as SingleTranslationState.Available
            assertEquals(testTranslation, availableState.translation)
            assertEquals("en", availableState.selectedLanguage)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits NotFound state when translation is not found`() = runTest {
        // Given
        coEvery { observeTranslationsUseCase(any()) } returns flowOf(
            SingleTranslationState.NotFound
        )

        // When
        viewModel = SingleTranslationViewModel("test.key", observeTranslationsUseCase, initStateManager)

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(SingleTranslationState.IsLoading, awaitItem())
            assertEquals(SingleTranslationState.NotFound, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits Error state when error occurs`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { observeTranslationsUseCase(any()) } returns flowOf(
            SingleTranslationState.Error(errorMessage)
        )

        // When
        viewModel = SingleTranslationViewModel("test.key", observeTranslationsUseCase, initStateManager)

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(SingleTranslationState.IsLoading, awaitItem())
            val errorState = awaitItem() as SingleTranslationState.Error
            assertEquals(errorMessage, errorState.message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `passes correct key name to use case`() = runTest {
        // Given
        val keyName = "specific.test.key"
        coEvery { observeTranslationsUseCase(keyName) } returns flowOf(
            SingleTranslationState.Available(
                testTranslation.copy(keyName = keyName),
                "en"
            )
        )

        // When
        viewModel = SingleTranslationViewModel(keyName, observeTranslationsUseCase, initStateManager)

        // Then
        viewModel.state.test(timeout = 5.seconds) {
            assertEquals(SingleTranslationState.IsLoading, awaitItem())
            val availableState = awaitItem() as SingleTranslationState.Available
            assertEquals(keyName, availableState.translation.keyName)
            cancelAndConsumeRemainingEvents()
        }
    }
} 