package cz.fit.cvut.sdk.config

import android.content.Context
import android.content.pm.PackageManager
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import kotlinx.coroutines.CoroutineScope

/**
 * Configuration class for TolgeeSdk
 */
data class TolgeeSdkConfig(
    val baseUrl: String,
    val apiKey: String,
    val context: Context,
    val mode: TolgeeSdkMode = TolgeeSdkMode.DEBUG,
    val coroutineScope: CoroutineScope? = null,
    val autoReleaseOnDestroy: Boolean = true
)

/**
 * Builder for TolgeeSdk configuration
 */
class TolgeeSdkConfigBuilder {
    var baseUrl: String? = null
    var apiKey: String? = null
    var context: Context? = null
    var mode: TolgeeSdkMode = TolgeeSdkMode.DEBUG
    var autoReleaseOnDestroy: Boolean = true
    
    /**
     * Build the configuration
     * @throws IllegalArgumentException if required parameters are missing
     */
    fun build(): TolgeeSdkConfig {
        requireNotNull(baseUrl) { "Base URL must be set" }
        requireNotNull(apiKey) { "API key must be set" }
        requireNotNull(context) { "Context must be set" }
        
        return TolgeeSdkConfig(
            baseUrl = baseUrl!!,
            apiKey = apiKey!!,
            context = context!!,
            mode = mode,
            autoReleaseOnDestroy = autoReleaseOnDestroy
        )
    }
}

/**
 * Utility for loading configuration from Android manifest metadata
 */
object TolgeeSdkConfigLoader {
    private const val META_BASE_URL = "cz.fit.cvut.sdk.BASE_URL"
    private const val META_API_KEY = "cz.fit.cvut.sdk.API_KEY"
    private const val META_MODE = "cz.fit.cvut.sdk.MODE"
    
    /**
     * Load configuration from manifest metadata
     * @param context Application context
     * @throws IllegalStateException if required metadata is missing
     */
    fun fromMetadata(context: Context): TolgeeSdkConfig {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName, 
            PackageManager.GET_META_DATA
        )
        
        val baseUrl = appInfo.metaData.getString(META_BASE_URL)
            ?: throw IllegalStateException("BASE_URL not found in metadata")
            
        val apiKey = appInfo.metaData.getString(META_API_KEY)
            ?: throw IllegalStateException("API_KEY not found in metadata")
            
        val modeString = appInfo.metaData.getString(META_MODE) ?: "DEBUG"
        val mode = try {
            TolgeeSdkMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            TolgeeSdkMode.DEBUG
        }
        
        return TolgeeSdkConfig(
            baseUrl = baseUrl,
            apiKey = apiKey,
            context = context,
            mode = mode
        )
    }
} 