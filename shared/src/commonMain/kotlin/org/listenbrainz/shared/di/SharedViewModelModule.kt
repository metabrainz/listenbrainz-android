package org.listenbrainz.shared.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.viewmodel.NewsListViewModel
import org.listenbrainz.shared.viewmodel.DashBoardViewModel
import org.listenbrainz.shared.viewmodel.FeaturesViewModel
import org.listenbrainz.shared.viewmodel.AlbumViewModel
import org.listenbrainz.shared.viewmodel.ArtistViewModel
import org.listenbrainz.shared.viewmodel.FeedViewModel
import org.listenbrainz.shared.viewmodel.LoginViewModel
import org.listenbrainz.shared.viewmodel.ListeningNowViewModel
import org.listenbrainz.shared.viewmodel.ListensViewModel
import org.listenbrainz.shared.viewmodel.PlaylistDataViewModel
import org.listenbrainz.shared.viewmodel.SearchViewModel
import org.listenbrainz.shared.viewmodel.SettingsViewModel
import org.listenbrainz.shared.viewmodel.UserViewModel
import org.listenbrainz.shared.viewmodel.SocialViewModel

val sharedViewModelModule = module {
    viewModel { DashBoardViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(),get()) }
    viewModel { NewsListViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { FeaturesViewModel(get()) }
    viewModel { ArtistViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { AlbumViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { ListensViewModel(get(),get(),get(),get(),get(named(DEFAULT_DISPATCHER))) }
    viewModel { ListeningNowViewModel(get(),get(),get(),get(named(IO_DISPATCHER))) }
    viewModel { PlaylistDataViewModel(get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { SocialViewModel(get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { FeedViewModel(get(),get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { SearchViewModel(get(),get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { UserViewModel(get(),get(),get(),get(),get(),get(named(IO_DISPATCHER))) }
    viewModel { LoginViewModel(get(),get(),get(named(IO_DISPATCHER))) }
}