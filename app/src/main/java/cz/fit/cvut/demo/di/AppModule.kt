package cz.fit.cvut.demo.di

import cz.fit.cvut.demo.data.AppDatabase
import cz.fit.cvut.demo.data.TodoRepository
import cz.fit.cvut.demo.screens.todo.TodoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.build(get()) }
    single { get<AppDatabase>().todoDao() }
    single { TodoRepository(get()) }

    viewModelOf(::TodoViewModel)
} 