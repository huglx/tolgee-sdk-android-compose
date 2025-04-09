package cz.fit.cvut.feature.translation.presentation.detail

import app.cash.turbine.test
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.core.data.db.TestTolgeeDB
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import cz.fit.cvut.feature.translation.data.api.dto.request.UpdateTranslationContextRequest
import cz.fit.cvut.feature.translation.data.api.dto.response.TranslationResponse
import cz.fit.cvut.feature.translation.data.api.dto.response.UpdateTranslationsContextResponse
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSourceImpl
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.feature.translation.domain.usecases.UpdateTranslationWithContextUseCase
import cz.fit.cvut.feature.translation.domain.models.TolgeeKeyModel
import cz.fit.cvut.feature.translation.domain.models.TolgeeTranslationModel
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.data.api.dto.response.EmbeddedLanguages
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguageDto
import cz.fit.cvut.feature.language.data.api.dto.response.TolgeeLanguagesResponse
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferences
import cz.fit.cvut.feature.language.data.source.LanguageLocalSourceImpl
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import cz.fit.cvut.feature.language.domain.models.TolgeeLanguageModel
import cz.fit.cvut.feature.translations_context.data.KeyMetaRepositoryImpl
import cz.fit.cvut.feature.translations_context.domain.usecase.FindNeighborKeysUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
class TranslationDetailsViewModelTest : TestTolgeeDB() {
    private lateinit var viewModel: TranslationDetailsViewModel
    private lateinit var translationsRemoteSource: TranslationsRemoteSource
    private lateinit var languageRemoteSource: LanguageRemoteSource
    private lateinit var translationsRepository: TranslationsRepository
    private lateinit var languageRepository: LanguageRepository
    private lateinit var updateWithContextUseCase: UpdateTranslationWithContextUseCase
    private lateinit var translationsLocalSource: TranslationsLocalSourceImpl
    private lateinit var languageLocalSource: LanguageLocalSourceImpl
    private lateinit var findNeighborKeysUseCase: FindNeighborKeysUseCase
    private val translationMapper = TolgeeEntityMapper()
    private val languageMapper = LanguageEntityMapper()
    private val testDispatcher = StandardTestDispatcher()

    private val testTranslations = mapOf(
        "en" to "Hello",
        "cs" to "Ahoj"
    )

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

    private val remoteLangResponse = TolgeeLanguagesResponse(
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup remote sources
        translationsRemoteSource = mockk()
        languageRemoteSource = mockk()
        findNeighborKeysUseCase = mockk()

        // Setup local sources with in-memory database
        translationsLocalSource = TranslationsLocalSourceImpl(db.tolgeeDao(), translationMapper)
        languageLocalSource = LanguageLocalSourceImpl(db.languageDao(), languageMapper)

        // Create mock language preferences
        val languagePreferences = mockk<LanguagePreferences>()
        coEvery { languagePreferences.selectedLanguage } returns flowOf("en")
        coEvery { languagePreferences.saveSelectedLanguage(any()) } returns Unit

        // Setup repositories
        languageRepository = LanguageRepository(
            languageRemoteSource = languageRemoteSource,
            languageLocalSource = languageLocalSource,
            langPreferences = languagePreferences
        )
        
        translationsRepository = TranslationsRepository(
            translationsRemoteSource = translationsRemoteSource,
            translationsLocalSource = translationsLocalSource,
            languageRepository = languageRepository
        )

        // Insert test languages into database
        runTest {
            languageLocalSource.saveLanguages(testLanguages)
        }

        // Setup language repository mock responses
        coEvery { languageRemoteSource.getLanguages() } returns remoteLangResponse

        // Setup language repository mock responses
        coEvery { findNeighborKeysUseCase(any()) } returns emptyList()

        updateWithContextUseCase = UpdateTranslationWithContextUseCase(translationsRepository, findNeighborKeysUseCase)

        viewModel = TranslationDetailsViewModel(
            updateNoContextUseCase = mockk(relaxed = true),
            updateWithContextUseCase = updateWithContextUseCase,
            translationsRepository = translationsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() = runTest {
        viewModel.updateState.test(timeout = 5.seconds) {
            assertEquals(UpdateState.Idle, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `updateTranslationWithContext updates existing key in database on success`() = runTest {
        // Given - insert initial key into database
        val keyName = "test.key"
        val keyId = 1L
        val initialKey = TolgeeKeyModel(
            keyId = keyId,
            keyName = keyName,
            translations = mapOf(
                "en" to TolgeeTranslationModel(1L,"Initial", testLanguages[0]),
                "cs" to TolgeeTranslationModel(2L,"Počáteční", testLanguages[1])
            )
        )
        translationsLocalSource.saveKey(initialKey)

        // Verify initial state in database
        val initialDbKey = translationsRepository.observeTranslationForKey(keyName).first()
        assertEquals("Initial", initialDbKey?.translations?.get("en")?.text)
        assertEquals("Počáteční", initialDbKey?.translations?.get("cs")?.text)
        assertEquals(testLanguages[0], initialDbKey?.translations?.get("en")?.language)
        assertEquals(testLanguages[1], initialDbKey?.translations?.get("cs")?.language)

        // Setup request and response
        val request = UpdateTranslationContextRequest(
            name = keyName,
            relatedKeysInOrder = emptyList(),
            translations = testTranslations
        )
        val response = UpdateTranslationsContextResponse(
            description = null,
            keyId = keyId,
            keyName = keyName,
            translations = mapOf(
                "en" to TranslationResponse(1, "Hello"),
                "cs" to TranslationResponse(2, "Ahoj")
            )
        )
        
        coEvery { 
            translationsRemoteSource.updateTranslationWithContext(request, keyId)
        } returns response

        // When - update the translation
        viewModel.updateState.test(timeout = 5.seconds) {
            assertEquals(UpdateState.Idle, awaitItem())
            viewModel.updateTranslationWithContext(keyName, testTranslations, keyId)
            assertEquals(UpdateState.Loading, awaitItem())
            assertEquals(UpdateState.Success, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        // Then - verify database was updated
        val updatedKey = translationsRepository.observeTranslationForKey(keyName).first()
        assertEquals("Hello", updatedKey?.translations?.get("en")?.text)
        assertEquals("Ahoj", updatedKey?.translations?.get("cs")?.text)
        assertEquals(testLanguages[0], updatedKey?.translations?.get("en")?.language)
        assertEquals(testLanguages[1], updatedKey?.translations?.get("cs")?.language)
    }

    @Test
    fun `updateTranslationWithContext creates new key in database on success`() = runTest {
        // Given
        val keyName = "new.key"
        val keyId = 1L
        
        // Verify key doesn't exist initially
        val initialKey = translationsRepository.observeTranslationForKey(keyName).first()
        assertTrue(initialKey == null)

        // Setup request and response
        val request = UpdateTranslationContextRequest(
            name = keyName,
            relatedKeysInOrder = emptyList(),
            translations = testTranslations
        )
        val response = UpdateTranslationsContextResponse(
            description = null,
            keyId = keyId,
            keyName = keyName,
            translations = mapOf(
                "en" to TranslationResponse(1, "Hello"),
                "cs" to TranslationResponse(2, "Ahoj")
            )
        )
        
        //coEvery { translationsRepository.observeTranslationForKey(keyName) } returns flowOf(null)
        coEvery { 
            translationsRemoteSource.createTranslationWithContext(request)
        } returns response

        // When - create the translation
        viewModel.updateState.test(timeout = 5.seconds) {
            assertEquals(UpdateState.Idle, awaitItem())
            viewModel.updateTranslationWithContext(keyName, testTranslations, keyId)
            assertEquals(UpdateState.Loading, awaitItem())
            assertEquals(UpdateState.Success, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        // Then - verify key was created in database with proper languages
        val createdKey = translationsRepository.observeTranslationForKey(keyName).first()
        assertEquals("Hello", createdKey?.translations?.get("en")?.text)
        assertEquals("Ahoj", createdKey?.translations?.get("cs")?.text)
        assertEquals(keyId, createdKey?.keyId)
        assertEquals(testLanguages[0], createdKey?.translations?.get("en")?.language)
        assertEquals(testLanguages[1], createdKey?.translations?.get("cs")?.language)
    }

    @Test
    fun `updateTranslationWithContext emits error state on failure`() = runTest {
        // Given
        val keyName = "test.key"
        val keyId = 1L
        val errorMessage = "Network error"
        val request = UpdateTranslationContextRequest(
            name = keyName,
            relatedKeysInOrder = emptyList(),
            translations = testTranslations
        )
        
        //coEvery { translationsRepository.observeTranslationForKey(keyName) } returns flowOf(null)
        coEvery { 
            translationsRemoteSource.createTranslationWithContext(request)
        } throws Exception(errorMessage)

        // When & Then
        viewModel.updateState.test(timeout = 5.seconds) {
            assertEquals(UpdateState.Idle, awaitItem())
            viewModel.updateTranslationWithContext(keyName, testTranslations, keyId)
            assertEquals(UpdateState.Loading, awaitItem())
            val error = awaitItem() as UpdateState.Error
            assertTrue(error.message.contains(errorMessage))
            cancelAndConsumeRemainingEvents()
        }

        // Verify database wasn't modified
        val keyAfterError = translationsRepository.observeTranslationForKey(keyName).first()
        assertTrue(keyAfterError == null)
    }
} 