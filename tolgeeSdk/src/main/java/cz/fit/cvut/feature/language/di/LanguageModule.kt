package cz.fit.cvut.feature.language.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.feature.language.data.LanguageRepository
import cz.fit.cvut.feature.language.data.api.LanguageApiDescription
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferences
import cz.fit.cvut.feature.language.data.datastore.LanguagePreferencesImpl
import cz.fit.cvut.feature.language.data.db.mapper.LanguageEntityMapper
import cz.fit.cvut.feature.language.data.source.LanguageLocalSource
import cz.fit.cvut.feature.language.data.source.LanguageLocalSourceImpl
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSource
import cz.fit.cvut.feature.language.data.source.LanguageRemoteSourceImpl
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit

internal val languageModule = module {
    // Database and API dependencies
    single { get<TolgeeDB>().languageDao() }
    single { get<Retrofit>().create(LanguageApiDescription::class.java) }
    
    // DataStore for language preferences
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("language_preferences")
        }
    }
    
    // Provide LanguagePreferencesImpl
    single { LanguagePreferencesImpl(get()) } bind LanguagePreferences::class
    
    // Repository - uses LanguagePreferences directly, no coroutine scope needed
    single { LanguageRepository(get(), get(), get()) }
    
    // Mappers
    single { LanguageEntityMapper() }
    
    // Data sources
    single<LanguageLocalSource> { LanguageLocalSourceImpl(get(), get()) }
    single<LanguageRemoteSource> { LanguageRemoteSourceImpl(get()) }

    // ViewModels
    singleOf(::TolgeeLanguageViewModel)
} 