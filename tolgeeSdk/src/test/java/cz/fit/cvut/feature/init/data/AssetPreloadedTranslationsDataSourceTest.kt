package cz.fit.cvut.feature.init.data

import android.content.Context
import android.content.res.AssetManager
import cz.fit.cvut.core.common.utils.ResultWrapper
import cz.fit.cvut.feature.init.data.source.AssetPreloadedTranslationsDataSource
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class AssetPreloadedTranslationsDataSourceTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var dataSource: AssetPreloadedTranslationsDataSource

    // Test SQL content
    private val testSqlContent = """
        INSERT OR REPLACE INTO tolgee_languages (languageId, languageName, originalName, languageTag, flagEmoji, isBase) VALUES (1, 'English', 'English', 'en', '🇺🇸', 1);
        INSERT OR REPLACE INTO tolgee_languages (languageId, languageName, originalName, languageTag, flagEmoji, isBase) VALUES (2, 'Čeština', 'Czech', 'cs', '🇨🇿', 0);
        
        INSERT OR REPLACE INTO tolgee_keys (keyId, keyName) VALUES (1, 'welcome');
        INSERT OR REPLACE INTO tolgee_keys (keyId, keyName) VALUES (2, 'goodbye');
        
        INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (1, 1, 1, 'Welcome');
        INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (2, 2, 1, 'Goodbye');
        INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (3, 1, 2, 'Vítejte');
        INSERT OR REPLACE INTO tolgee_translations (translationId, keyId, languageId, translationText) VALUES (4, 2, 2, 'Na shledanou');
    """.trimIndent()

    @Before
    fun setup() {
        context = mockk()
        assetManager = mockk()

        every { context.assets } returns assetManager

        dataSource = AssetPreloadedTranslationsDataSource(context, TolgeeSdkMode.RELEASE)
    }

    @Test
    fun `isAvailable returns false when not in RELEASE mode`() {
        // Arrange
        val debugDataSource = AssetPreloadedTranslationsDataSource(context, TolgeeSdkMode.DEBUG)

        // Act
        val result = debugDataSource.isAvailable()

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `isAvailable returns false when SQL file does not exist`() {
        // Arrange
        every { assetManager.open("preloaded_translations.sql") } throws IOException("File not found")

        // Act
        val result = dataSource.isAvailable()

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `isAvailable returns true when in RELEASE mode and SQL file exists`() {
        // Arrange
        val inputStream = mockk<InputStream>()
        every { assetManager.open("preloaded_translations.sql") } returns inputStream
        every { inputStream.close() } just Runs

        // Act
        val result = dataSource.isAvailable()

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `getSqlStatements returns error when not available`() = runBlocking {
        // Arrange
        val debugDataSource = AssetPreloadedTranslationsDataSource(context, TolgeeSdkMode.DEBUG)

        // Act
        val result = debugDataSource.getSqlStatements()

        // Assert
        assertTrue(result is ResultWrapper.Error)
    }

    @Test
    fun `getSqlStatements returns SQL statements when available`() = runBlocking {
        // Arrange
        val inputStream = ByteArrayInputStream(testSqlContent.toByteArray())
        every { assetManager.open("preloaded_translations.sql") } returns inputStream

        // Act
        val result = dataSource.getSqlStatements()

        // Assert
        assertTrue(result is ResultWrapper.Success)
        result as ResultWrapper.Success
        assertEquals(8, result.data.size) // 8 statements in our test SQL
    }

    @Test
    fun `getSqlStatements adds semicolons at the end of each statement`() = runBlocking {
        // Arrange
        val inputStream = ByteArrayInputStream(testSqlContent.toByteArray())
        every { assetManager.open("preloaded_translations.sql") } returns inputStream

        // Act
        val result = dataSource.getSqlStatements()

        // Assert
        assertTrue(result is ResultWrapper.Success)
        result as ResultWrapper.Success
        val statements = result.data

        // Each statement should end with a semicolon
        statements.forEach { statement ->
            assertTrue(statement.trim().endsWith(";"))
        }
    }
}