package cz.fit.cvut.sdk.core

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import cz.fit.cvut.sdk.config.TolgeeSdkConfig
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import cz.fit.cvut.sdk.config.TolgeeSdkConfigBuilder
import cz.fit.cvut.sdk.config.TolgeeSdkConfigLoader

/**
 * Singleton class for managing TolgeeSdk initialization and configuration
 */
object TolgeeSdkInitializer {
    private var instance: TolgeeSdk? = null
    
    /**
     * Initialize TolgeeSdk with configuration
     * @param application Application context
     * @param config Configuration for the SDK
     */
    private fun initialize(application: Application, config: TolgeeSdkConfig) {
        if (instance == null) {
            instance = TolgeeSdkFactory.create(config)
        }
    }
    
    /**
     * Initialize TolgeeSdk with configuration block
     * @param application Application context
     * @param block Configuration block
     */
    private fun initialize(application: Application, block: TolgeeSdkConfigBuilder.() -> Unit) {
        if (instance == null) {
            val builder = TolgeeSdkConfigBuilder().apply {
                context = application
                block()
            }
            instance = TolgeeSdkFactory.create(builder.build())
        }
    }
    
    /**
     * Initialize TolgeeSdk from manifest metadata
     * @param application Application context
     */
    fun initializeFromMetadata(application: Application) {
        if (instance == null) {
            val config = TolgeeSdkConfigLoader.fromMetadata(application)
            initialize(application, config)
        }
    }
    
    /**
     * Get the initialized TolgeeSdk instance
     * @throws IllegalStateException if SDK is not initialized
     */
    fun getInstance(): TolgeeSdk {
        return instance ?: throw IllegalStateException("TolgeeSdk not initialized. Call initialize() first.")
    }
} 