package cz.fit.cvut.sdk.extensions

import androidx.navigation.NavController
import cz.fit.cvut.sdk.utils.RouteProviderRegistry

/**
 * Extension functions for NavController integration with Tolgee SDK
 */
fun NavController.registerAsRouteProviderForTolgee() {
    RouteProviderRegistry.registerRouteProvider {
        this.currentDestination?.route
    }
}

/**
 * Unregister this NavController as the route provider
 */
fun NavController.unregisterAsRouteProviderForTolgee() {
    RouteProviderRegistry.clearRouteProvider()
} 