package cz.fit.cvut.core.common.di

import android.content.Context
import cz.fit.cvut.feature.init.di.initModule
import cz.fit.cvut.feature.language.di.languageModule
import cz.fit.cvut.feature.navigation.di.navigationModule
import cz.fit.cvut.feature.translation.di.translationModel
import cz.fit.cvut.feature.translations_context.di.translationsContextModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.dsl.koinApplication

internal object IsolatedKoinContext {
    private var _koinApp: KoinApplication? = null
    private val koinApp: KoinApplication
        get() = _koinApp ?: error("KoinApplication has not been started")

    val koin: Koin
        get() = koinApp.koin

    @Synchronized
    fun start(context: Context) {
        if (_koinApp == null) {
            _koinApp = koinApplication {
                androidContext(context.applicationContext)
                modules(listOf(coreModule, translationModel, initModule, languageModule, navigationModule, translationsContextModule))
            }
        }
    }
}

internal interface TolgeeKoinComponent: KoinComponent {
    override fun getKoin() = IsolatedKoinContext.koin
}

