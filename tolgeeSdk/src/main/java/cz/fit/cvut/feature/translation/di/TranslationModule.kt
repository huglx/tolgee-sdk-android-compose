package cz.fit.cvut.feature.translation.di

import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSource
import cz.fit.cvut.feature.translation.data.api.TranslationsApiDescription
import cz.fit.cvut.feature.translation.data.source.TranslationsRemoteSourceImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import cz.fit.cvut.feature.translation.domain.usecases.GetTranslationsForAvailableLanguagesUseCase
import cz.fit.cvut.feature.translation.data.TranslationsRepository
import org.koin.core.module.dsl.viewModelOf
import cz.fit.cvut.feature.translation.presentation.list.TranslationsListViewModel
import cz.fit.cvut.feature.translation.presentation.detail.TranslationDetailsViewModel
import cz.fit.cvut.feature.translation.domain.usecases.UpdateTranslationNoContextUseCase
import cz.fit.cvut.feature.translation.domain.usecases.UpdateTranslationWithContextUseCase
import org.koin.core.module.dsl.singleOf
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSource
import cz.fit.cvut.feature.translation.data.source.TranslationsLocalSourceImpl
import cz.fit.cvut.feature.translation.data.db.mapper.TolgeeEntityMapper
import cz.fit.cvut.core.data.db.TolgeeDB
import cz.fit.cvut.feature.translation.domain.usecases.ObserveTranslationsUseCase
import cz.fit.cvut.feature.translation.presentation.common.viewmodel.SingleTranslationViewModel
import org.koin.core.module.dsl.viewModel
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigationViewModel

internal val translationModel get() = module {
    single { get<TolgeeDB>().tolgeeDao() }
    single { get<Retrofit>().create(TranslationsApiDescription::class.java) }

    factory<TolgeeEntityMapper> { TolgeeEntityMapper() }
    factory<TranslationsLocalSource> { TranslationsLocalSourceImpl(get(), get()) }
    factory<TranslationsRemoteSource> { TranslationsRemoteSourceImpl(apiDescription = get()) }
    singleOf(::TranslationsRepository)
    
    // Single Translation ViewModel
    viewModel { parameters -> 
        SingleTranslationViewModel(
            keyName = parameters.get(),
            observeTranslationsUseCase = get(),
            get()
        )
    }
    
    // Other ViewModels
    factoryOf(::GetTranslationsForAvailableLanguagesUseCase)
    factoryOf(::UpdateTranslationNoContextUseCase)
    factoryOf(::ObserveTranslationsUseCase)
    factoryOf(::UpdateTranslationWithContextUseCase)
    viewModelOf(::TranslationDetailsViewModel)
    viewModelOf(::TranslationsListViewModel)
}