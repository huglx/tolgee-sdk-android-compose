package cz.fit.cvut.sdk.core

import cz.fit.cvut.sdk.config.TolgeeSdkConfig
import cz.fit.cvut.sdk.config.TolgeeSdkConfigBuilder
import cz.fit.cvut.sdk.manager.TolgeeSdkInstanceManager

/**
 * Factory for creating TolgeeSdk instances
 */
object TolgeeSdkFactory {
    /**
     * Creates a new TolgeeSdk instance
     * @param config The configuration for the SDK
     * @return A new TolgeeSdk instance
     */
    fun create(config: TolgeeSdkConfig): TolgeeSdk {
        return TolgeeSdk(config, instanceManager = TolgeeSdkInstanceManager())
    }
    
    /**
     * Creates a new TolgeeSdk instance using the builder pattern
     * @param block Configuration block to set up the SDK
     * @return A new TolgeeSdk instance
     */
    fun create(block: TolgeeSdkConfigBuilder.() -> Unit): TolgeeSdk {
        val builder = TolgeeSdkConfigBuilder()
        builder.apply(block)
        return create(builder.build())
    }
    
    /**
     * Creates a TolgeeSdk instance with a specific instance manager
     * For internal use
     */
    internal fun createWithManager(config: TolgeeSdkConfig, manager: TolgeeSdkInstanceManager): TolgeeSdk {
        return TolgeeSdk(config, manager)
    }
}

/**
 * DSL function for creating and configuring the SDK
 */
fun tolgee(block: TolgeeSdkConfigBuilder.() -> Unit): TolgeeSdk {
    return TolgeeSdkFactory.create(block)
} 