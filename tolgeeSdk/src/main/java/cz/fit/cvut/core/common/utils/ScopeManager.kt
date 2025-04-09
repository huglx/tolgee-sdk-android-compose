package cz.fit.cvut.core.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Manager for coroutine scopes in the SDK
 */
internal class ScopeManager {
    private val mainScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob()) }
    private val ioScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    private val defaultScope by lazy { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    
    /**
     * Get the main dispatcher scope
     */
    fun mainScope(): CoroutineScope = mainScope
    
    /**
     * Get the IO dispatcher scope
     */
    fun ioScope(): CoroutineScope = ioScope
    
    /**
     * Get the default dispatcher scope
     */
    fun defaultScope(): CoroutineScope = defaultScope
    
    /**
     * Cancel all scopes when SDK is being released
     */
    fun disposeScopes() {
        mainScope.cancel("SDK is being released")
        ioScope.cancel("SDK is being released")
        defaultScope.cancel("SDK is being released")
    }
} 