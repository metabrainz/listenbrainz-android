package org.listenbrainz.shared.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.listenbrainz.shared.viewmodel.SettingsViewModel

val sharedViewModelModule = module {
    viewModel { SettingsViewModel(get()) }
}