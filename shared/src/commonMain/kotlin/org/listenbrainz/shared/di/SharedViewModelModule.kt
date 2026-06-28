package org.listenbrainz.shared.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.util.StringProvider
import org.listenbrainz.shared.viewmodel.NewsListViewModel
import org.listenbrainz.shared.viewmodel.CreateAccountViewModel
import org.listenbrainz.shared.viewmodel.FeaturesViewModel
import org.listenbrainz.shared.viewmodel.AlbumViewModel
import org.listenbrainz.shared.viewmodel.ArtistViewModel
import org.listenbrainz.shared.viewmodel.BPArtistViewModel
import org.listenbrainz.shared.viewmodel.FeedViewModel
import org.listenbrainz.shared.viewmodel.LoginViewModel
import org.listenbrainz.shared.viewmodel.ListeningNowViewModel
import org.listenbrainz.shared.viewmodel.ListensViewModel
import org.listenbrainz.shared.viewmodel.PlaylistViewModel
import org.listenbrainz.shared.viewmodel.BPAlbumViewModel
import org.listenbrainz.shared.viewmodel.PlaylistDataViewModel
import org.listenbrainz.shared.viewmodel.SearchViewModel
import org.listenbrainz.shared.viewmodel.SettingsViewModel
import org.listenbrainz.shared.viewmodel.SongViewModel
import org.listenbrainz.shared.viewmodel.SocialViewModel

val sharedViewModelModule = module {
    viewModel { SettingsViewModel(get(),get()) }
    viewModel { NewsListViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { CreateAccountViewModel(get()) }
    viewModel { FeaturesViewModel(get()) }
    viewModel { ArtistViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { LoginViewModel(get()) }
    viewModel { AlbumViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { ListensViewModel(get(),get(),get(),get(),get(named(DEFAULT_DISPATCHER))) }
    viewModel { SongViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { PlaylistViewModel(get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { BPAlbumViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { BPArtistViewModel(get(),get(named(IO_DISPATCHER))) }
    viewModel { ListensViewModel(get(),get(),get(),get(),get(named(DEFAULT_DISPATCHER))) }
    viewModel { ListeningNowViewModel(get(),get(),get(),get(named(IO_DISPATCHER))) }
    viewModel { PlaylistDataViewModel(get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER)),get<StringProvider>()) }
    viewModel { SocialViewModel(get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER)),get<StringProvider>()) }
    viewModel { FeedViewModel(get(),get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { PlaylistDataViewModel(get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER)),get<StringProvider>()) }
    viewModel { SearchViewModel(get(),get(),get(),get(),get(),get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
}