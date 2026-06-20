package org.listenbrainz.shared.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.viewmodel.CreateAccountViewModel
import org.listenbrainz.shared.viewmodel.ArtistViewModel
import org.listenbrainz.shared.viewmodel.SettingsViewModel

val sharedViewModelModule = module {
    viewModel { SettingsViewModel(get(),get()) }
    viewModel { CreateAccountViewModel(get()) }
    viewModel { ArtistViewModel(get(),get(named(IO_DISPATCHER))) }
}