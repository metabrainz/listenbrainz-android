package org.listenbrainz.shared.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.repository.blog.BlogRepository
import org.listenbrainz.shared.repository.blog.BlogRepositoryImpl
import org.listenbrainz.shared.repository.album.AlbumRepository
import org.listenbrainz.shared.repository.album.AlbumRepositoryImpl
import org.listenbrainz.shared.repository.artist.ArtistRepository
import org.listenbrainz.shared.repository.artist.ArtistRepositoryImpl
import org.listenbrainz.shared.repository.socket.SocketRepository
import org.listenbrainz.shared.repository.socket.SocketRepositoryImpl

val sharedRepositoryModule = module {
    single<SocketRepository> { SocketRepositoryImpl(get<HttpClient>(named(SHARED_HTTP_CLIENT)),get()) }
    single<BlogRepository> { BlogRepositoryImpl(get()) }
    single<ArtistRepository> { ArtistRepositoryImpl(get(),get(),get()) }
    single<AlbumRepository> { AlbumRepositoryImpl(get(),get(),get()) }
}