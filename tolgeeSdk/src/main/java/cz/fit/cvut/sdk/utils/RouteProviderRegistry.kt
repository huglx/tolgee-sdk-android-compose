package cz.fit.cvut.sdk.utils

import cz.fit.cvut.sdk.core.TolgeeSdkInitializer

/**
 * Registry for managing route providers in the Tolgee SDK
 * This allows flexible registration of route providers from different navigation systems
 */
object RouteProviderRegistry {
    /**
     * Register a route provider function
     * @param routeProvider Function that returns the current route string
     */
    fun registerRouteProvider(routeProvider: () -> String?) {
        val sdk = TolgeeSdkInitializer.getInstance()
        sdk.setRouteProvider(routeProvider)
    }
    
    /**
     * Clear the current route provider
     */
    fun clearRouteProvider() {
        val sdk = TolgeeSdkInitializer.getInstance()
        sdk.setRouteProvider { null }
    }
} 