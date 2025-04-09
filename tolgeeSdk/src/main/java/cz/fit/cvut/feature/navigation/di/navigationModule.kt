package cz.fit.cvut.feature.navigation.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import cz.fit.cvut.feature.navigation.presentation.TranslationNavigationViewModel

internal val navigationModule get() = module {
    // Navigation ViewModel
    singleOf(::TranslationNavigationViewModel)

}