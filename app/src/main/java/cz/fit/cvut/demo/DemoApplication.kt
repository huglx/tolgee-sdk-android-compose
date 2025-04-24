package cz.fit.cvut.demo

import android.app.Application
import cz.fit.cvut.demo.di.appModule
import cz.fit.cvut.sdk.core.TolgeeSdkInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin
        startKoin {
            androidContext(this@DemoApplication)
            modules(appModule)
        }
        
        // Initialize Tolgee SDK from metadata
        TolgeeSdkInitializer.initializeFromMetadata(this)
    }
} 