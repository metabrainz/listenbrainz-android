package org.listenbrainz.shared.di

import org.koin.dsl.module
import org.listenbrainz.shared.repository.artist.ArtistRepository
import org.listenbrainz.shared.repository.artist.ArtistRepositoryImpl

val sharedRepositoryModule = module {
    single<ArtistRepository> { ArtistRepositoryImpl(get(),get(),get()) }
}