package cz.fit.cvut.sdk.core

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import cz.fit.cvut.core.common.di.IsolatedKoinContext
import cz.fit.cvut.core.common.utils.ScopeManager
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.feature.translations_context.utils.RouteManager
import cz.fit.cvut.sdk.manager.TolgeeSdkInstanceManager
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import cz.fit.cvut.sdk.config.TolgeeSdkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.Koin

/**
 * TolgeeSdk - main class for Tolgee translation integration
 */
class TolgeeSdk internal constructor(
    private val config: TolgeeSdkConfig,
    private val instanceManager: TolgeeSdkInstanceManager? = null
) : DefaultLifecycleObserver {
    private lateinit var koinInstance: Koin
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    val mode: TolgeeSdkMode
        get() = config.mode

    /**
     * Initializes SDK with configuration settings
     */
    init {
        initializeKoin()

        // Register lifecycle observer if the context is a LifecycleOwner
        if (config.context is LifecycleOwner) {
            (config.context as LifecycleOwner).lifecycle.addObserver(this)
        }
    }

    private fun initializeKoin() {
        IsolatedKoinContext.start(config.context)

        koinInstance = IsolatedKoinContext.koin
        koinInstance.setProperty("tolgeeSdk:baseUrl", config.baseUrl)
        koinInstance.setProperty("tolgeeSdk:apiKey", config.apiKey)
        koinInstance.setProperty("tolgeeSdk:mode", config.mode)
    }

    /**
     * Called when the lifecycle owner is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        release()
        // Remove the observer to prevent memory leaks
        owner.lifecycle.removeObserver(this)
    }

    /**
     * Release all SDK resources
     */
    fun release() {
        _isEditing.value = false
        try {
            // Get ScopeManager instance from Koin
            val scopeManager = koinInstance.get<ScopeManager>()
            scopeManager.disposeScopes()

            // Close database connection if needed
            val db = koinInstance.get<TolgeeDB>()
            if (db.isOpen) {
                db.close()
            }
        } catch (e: Exception) {
            // Log error but continue cleanup
        }

        // Remove from instance manager if available
        instanceManager?.deregisterInstance(this)
    }

    /**
     * Returns Koin instance for dependency access
     */
    internal fun getKoin(): Koin {
        check(::koinInstance.isInitialized) { "Koin is not initialized. Call init() first." }
        return koinInstance
    }

    /**
     * Toggles translation editing mode
     */
    internal fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    /**
     * Set route provider for the SDK
     * @param routeProvider Function that returns current route
     */
    fun setRouteProvider(routeProvider: () -> String?) {
        RouteManager.setRouteProvider(routeProvider)
    }
}