package cz.fit.cvut.core.common.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import cz.fit.cvut.core.data.api.TolgeeRetrofitProvider
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.sdk.utils.TolgeeSdkMode
import cz.fit.cvut.core.common.utils.InitStateManager
import cz.fit.cvut.core.common.utils.ScopeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Core module providing fundamental dependencies for the SDK
 */
internal val coreModule = module {
    // Scope Manager
    single { ScopeManager() }
    
    // Coroutine Scopes
    single(named("mainScope")) { get<ScopeManager>().mainScope() }
    single(named("ioScope")) { get<ScopeManager>().ioScope() }
    single(named("defaultScope")) { get<ScopeManager>().defaultScope() }
    
    // SDK Mode
    single<TolgeeSdkMode> { getProperty("tolgeeSdk:mode") }
    
    // Network
    single { TolgeeRetrofitProvider.provide(
        apiKey = getProperty("tolgeeSdk:apiKey"), 
        baseUrl = getProperty("tolgeeSdk:baseUrl")
    ) }
    
    // Database
    single { TolgeeDB.instance(androidContext()) }

    // Init State Manager
    single { InitStateManager() }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("tolgee_preferences")
        }
    }
}