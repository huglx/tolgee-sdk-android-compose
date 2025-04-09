package cz.fit.cvut.feature.init.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import cz.fit.cvut.feature.init.data.InitRepositoryImpl
import cz.fit.cvut.feature.init.data.source.AssetPreloadedTranslationsDataSource
import cz.fit.cvut.feature.init.data.source.PreloadedTranslationsDataSource
import cz.fit.cvut.feature.init.domain.InitRepository
import cz.fit.cvut.feature.init.presentation.viewmodel.TolgeeInitViewModel
import cz.fit.cvut.feature.init.data.datastore.PreloadedTranslationsPreferences
import cz.fit.cvut.feature.init.data.datastore.PreloadedTranslationsPreferencesImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.named

internal val initModule = module {
    // DataStore for preloaded translations preferences
    single<DataStore<Preferences>>(named("preloadedTranslationsDataStore")) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("preloaded_translations_preferences")
        }
    }
    
    // Provide PreloadedTranslationsPreferencesImpl
    single { PreloadedTranslationsPreferencesImpl(get(named("preloadedTranslationsDataStore"))) } bind PreloadedTranslationsPreferences::class
    
    factory<PreloadedTranslationsDataSource> {
        AssetPreloadedTranslationsDataSource(androidContext(), get())
    }

    // Provide repository
    factory<InitRepository> {
        InitRepositoryImpl(
            languageRepository = get(),
            translationsRepository = get(),
            preloadedTranslationsDataSource = get(),
            database = get(),
            initStateManager = get(),
            preloadedTranslationsPreferences = get()
        )
    }
    
    single { TolgeeInitViewModel(get()) }
} 