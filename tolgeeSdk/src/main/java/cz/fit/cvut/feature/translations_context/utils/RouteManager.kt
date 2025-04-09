package cz.fit.cvut.feature.translations_context.utils

/**
 * Route manager for getting navigation route information from the app
 */
internal object RouteManager {
    // Route provider function type
    private var routeProviderFunction: (() -> String?)? = null
    
    /**
     * Set function that will provide current route
     * @param provider Function that returns current route or null
     */
    fun setRouteProvider(provider: () -> String?) {
        routeProviderFunction = provider
    }
    
    /**
     * Get current route using registered provider function
     * @return Current route or null if provider is not set
     */
    fun getCurrentRoute(): String? {
        return routeProviderFunction?.invoke() ?: "default_screen"
    }
} 