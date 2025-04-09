package cz.fit.cvut.sdk

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import cz.fit.cvut.feature.translations_context.utils.RouteManager
import cz.fit.cvut.sdk.core.TolgeeSdkFactory
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TolgeeSdkTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockLifecycleOwner: LifecycleOwner
    private lateinit var mockLifecycle: Lifecycle
    
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        
        // Create separate mocks
        mockLifecycle = mockk(relaxed = true)
        mockLifecycleOwner = mockk(relaxed = true)
        every { mockLifecycleOwner.lifecycle } returns mockLifecycle
    }
    
    @Test
    fun `test SDK initialization with different configurations`() {
        // When
        val debugSdk = TolgeeSdkFactory.create {
            baseUrl = "https://debug.example.com"
            apiKey = "debug-key"
            context = mockContext
            mode = TolgeeSdkMode.DEBUG
        }
        
        val releaseSdk = TolgeeSdkFactory.create {
            baseUrl = "https://release.example.com"
            apiKey = "release-key"
            context = mockContext
            mode = TolgeeSdkMode.RELEASE
        }
        
        // Then
        assertEquals(TolgeeSdkMode.DEBUG, debugSdk.mode)
        assertEquals(TolgeeSdkMode.RELEASE, releaseSdk.mode)
    }
    
    @Test
    fun `test route provider configuration`() {
        // Given
        val sdk = TolgeeSdkFactory.create {
            baseUrl = "https://test.example.com"
            apiKey = "test-api-key"
            context = mockContext
        }
        
        // When
        sdk.setRouteProvider { "test-route" }
        
        // Then
        assertEquals("test-route", RouteManager.getCurrentRoute())
    }
    
    @Test
    fun `test editing state management`() {
        // Given
        val sdk = TolgeeSdkFactory.create {
            baseUrl = "https://test.example.com"
            apiKey = "test-api-key"
            context = mockContext
            mode = TolgeeSdkMode.DEBUG
        }
        
        // When
        sdk.toggleEditing()
        
        // Then
        assertEquals(true, sdk.isEditing.value)
        
        // When
        sdk.toggleEditing()
        
        // Then
        assertEquals(false, sdk.isEditing.value)
    }
    
    @Test
    fun `test lifecycle owner destruction triggers release`() {
        // Given
        val sdk = spyk(
            TolgeeSdkFactory.create {
                baseUrl = "https://test.example.com"
                apiKey = "test-api-key"
                context = mockContext
                autoReleaseOnDestroy = true
            }
        )
        
        // When
        sdk.onDestroy(mockLifecycleOwner)
        
        // Then
        verify(exactly = 1) {
            sdk.release()
        }
    }
} 