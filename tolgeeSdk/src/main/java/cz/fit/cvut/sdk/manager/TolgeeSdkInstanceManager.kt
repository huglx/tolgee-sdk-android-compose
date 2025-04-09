package cz.fit.cvut.sdk.manager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import cz.fit.cvut.sdk.core.TolgeeSdkFactory
import cz.fit.cvut.sdk.config.TolgeeSdkConfig
import cz.fit.cvut.sdk.config.TolgeeSdkConfigBuilder
import cz.fit.cvut.sdk.core.TolgeeSdk
import java.lang.ref.WeakReference

/**
 * Class that manages TolgeeSdk instances
 */
class TolgeeSdkInstanceManager : DefaultLifecycleObserver {
    private val activeInstances = mutableListOf<WeakReference<TolgeeSdk>>()
    private var lifecycleObserverRegistered = false

    init {
        // Register a process-level lifecycle observer to clean up when app is terminated
        synchronized(this) {
            if (!lifecycleObserverRegistered) {
                try {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(this)
                    lifecycleObserverRegistered = true
                } catch (e: Exception) {
                    // In some cases ProcessLifecycleOwner might not be available
                    // This is ok, as users can manually call release()
                }
            }
        }
    }

    /**
     * Cleanup all instances when app is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        cleanupAllInstances()
        super.onDestroy(owner)
    }

    /**
     * Cleanup all instances that are still referenced
     */
    fun cleanupAllInstances() {
        synchronized(activeInstances) {
            // Call release on all active instances
            activeInstances.forEach { weakRef ->
                weakRef.get()?.release()
            }
            // Clear the list
            activeInstances.clear()
        }
    }

    /**
     * Creates a new SDK instance with configuration
     */
    fun createInstance(config: TolgeeSdkConfig): TolgeeSdk {
        return TolgeeSdkFactory.createWithManager(config, this).also { sdk ->
            // Register the new instance
            if (config.autoReleaseOnDestroy) {
                synchronized(activeInstances) {
                    // Clean up any stale references
                    activeInstances.removeAll { weakRef -> weakRef.get() == null }
                    // Add the new instance
                    activeInstances.add(WeakReference(sdk))
                }
            }
        }
    }

    /**
     * Creates a new SDK instance using the builder pattern
     */
    fun createInstance(block: TolgeeSdkConfigBuilder.() -> Unit): TolgeeSdk {
        val builder = TolgeeSdkConfigBuilder()
        builder.apply(block)
        return createInstance(builder.build())
    }

    /**
     * Deregister an SDK instance from tracking
     */
    internal fun deregisterInstance(sdk: TolgeeSdk) {
        synchronized(activeInstances) {
            activeInstances.removeAll { weakRef -> weakRef.get() === sdk }
        }
    }
}