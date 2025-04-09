package cz.fit.cvut.feature.translations_context.di

import cz.fit.cvut.feature.translations_context.data.KeyMetaRepositoryImpl
import cz.fit.cvut.feature.translations_context.domain.repository.KeyMetaRepository
import cz.fit.cvut.feature.translations_context.domain.usecase.FindNeighborKeysUseCase
import cz.fit.cvut.feature.translations_context.presentation.viewmodel.TranslationScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal val translationsContextModule = module {
    // Repositories
    single { KeyMetaRepositoryImpl(scope = get(named("mainScope"))) } bind KeyMetaRepository::class
    
    // Use Cases
    factoryOf(::FindNeighborKeysUseCase)
    
    // View Models
    singleOf(::TranslationScannerViewModel)
} 