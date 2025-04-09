package cz.fit.cvut.feature.translations_context.data

import androidx.compose.ui.geometry.Rect
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeyMetaRepositoryImplTest {

    private lateinit var repository: KeyMetaRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        testScope = TestScope(testDispatcher) // Create a CoroutineScope with the TestDispatcher
        repository = KeyMetaRepositoryImpl(testScope)
    }

    @Test
    fun `registerKeyPosition should store key metadata with correct values`() {
        // Arrange
        val keyName = "test.key"
        val keyId = 1L
        val rect = Rect(10f, 20f, 110f, 120f)
        val description = "Test description"
        val screenId = "test_screen"

        // Act
        repository.registerKeyPosition(keyId, rect, screenId)

        // Assert
        val result = repository.findKeyPosition(keyId)
        assertEquals(keyId, result?.keyId)
        assertEquals(rect, result?.position)
        assertEquals(screenId, result?.screenId)
    }

    @Test
    fun `registerKeyPosition should update existing key if it already exists`() {
        // Arrange
        val keyName = "test.key"
        val keyId = 1L
        val initialRect = Rect(10f, 20f, 110f, 120f)
        val updatedRect = Rect(30f, 40f, 130f, 140f)
        val initialScreenId = "screen1"
        val updatedScreenId = "screen2"

        // Act
        repository.registerKeyPosition(keyId, initialRect, initialScreenId)
        repository.registerKeyPosition(keyId, updatedRect, updatedScreenId)

        // Assert
        val result = repository.findKeyPosition(keyId)
        assertEquals(keyId, result?.keyId)
        assertEquals(updatedRect, result?.position)
        assertEquals(updatedScreenId, result?.screenId)
    }

    @Test
    fun `getKeysByScreen should return all keys for a given screen`() {
        // Arrange
        val screen1 = "screen1"
        val screen2 = "screen2"

        repository.registerKeyPosition(1L, Rect(10f, 20f, 110f, 120f), screen1)
        repository.registerKeyPosition(2L, Rect(30f, 40f, 130f, 140f), screen1)
        repository.registerKeyPosition(3L, Rect(50f, 60f, 150f, 160f), screen2)

        // Act
        val screen1Keys = repository.getKeysByScreen(screen1)
        val screen2Keys = repository.getKeysByScreen(screen2)

        // Assert
        assertEquals(2, screen1Keys.size)
        assertEquals(1, screen2Keys.size)
        assertTrue(screen1Keys.any { it.keyId == 1L })
        assertTrue(screen1Keys.any { it.keyId == 2L })
        assertTrue(screen2Keys.any { it.keyId == 3L })
    }

    @Test
    fun `clearAllPositions should remove all stored key positions`() {
        // Arrange
        repository.registerKeyPosition(1L, Rect(10f, 20f, 110f, 120f), "screen1")
        repository.registerKeyPosition(2L, Rect(30f, 40f, 130f, 140f), "screen1")

        // Act
        repository.clearAllPositions()

        // Assert
        assertEquals(null, repository.findKeyPosition(1L))
        assertEquals(null, repository.findKeyPosition(2L))
        assertEquals(0, repository.getKeysByScreen("screen1").size)
    }
}