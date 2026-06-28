package org.listenbrainz.shared.di

import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.repository.blog.BlogRepository
import org.listenbrainz.shared.repository.blog.BlogRepositoryImpl
import org.listenbrainz.shared.repository.album.AlbumRepository
import org.listenbrainz.shared.repository.album.AlbumRepositoryImpl
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.provideListensRepositoryImpl
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.artist.ArtistRepository
import org.listenbrainz.shared.repository.artist.ArtistRepositoryImpl
import org.listenbrainz.shared.repository.feed.FeedRepository
import org.listenbrainz.shared.repository.feed.FeedRepositoryImpl
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.socket.SocketRepository
import org.listenbrainz.shared.repository.socket.SocketRepositoryImpl
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.repository.brainzplayer.SongRepository
import org.listenbrainz.shared.repository.brainzplayer.SongRepositoryImpl
import org.listenbrainz.shared.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.shared.repository.brainzplayer.PlaylistRepositoryImpl
import org.listenbrainz.shared.repository.playlists.PlaylistDataRepository
import org.listenbrainz.shared.repository.playlists.PlaylistDataRepositoryImpl
import org.listenbrainz.shared.repository.social.SocialRepository
import org.listenbrainz.shared.repository.social.SocialRepositoryImpl
import org.listenbrainz.shared.repository.brainzplayer.BPAlbumRepository
import org.listenbrainz.shared.repository.brainzplayer.BPAlbumRepositoryImpl
import org.listenbrainz.shared.repository.brainzplayer.BPArtistRepository
import org.listenbrainz.shared.repository.brainzplayer.BPArtistRepositoryImpl
import org.listenbrainz.shared.repository.user.UserRepository
import org.listenbrainz.shared.repository.user.UserRepositoryImpl

val sharedRepositoryModule = module {
    single<SocketRepository> { SocketRepositoryImpl(get<HttpClient>(named(SHARED_HTTP_CLIENT)),get()) }
    single<BlogRepository> { BlogRepositoryImpl(get()) }
    single<ArtistRepository> { ArtistRepositoryImpl(get(),get(),get()) }
    single<AlbumRepository> { AlbumRepositoryImpl(get(),get(),get()) }
    single<ListensRepository> {
        provideListensRepositoryImpl(
            get<ListensService>(),
            get<AppPreferences>(),
            get<UserService>(),
            get<PendingListensDao>(),
            get(
                named(IO_DISPATCHER)
            )
        )
    }
    single<SongRepository> { SongRepositoryImpl(get(),get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    single<SocialRepository> { SocialRepositoryImpl(get(),get()) }
    single<BPAlbumRepository> { BPAlbumRepositoryImpl(get(),get(),get()) }
    single<BPArtistRepository> { BPArtistRepositoryImpl(get(),get(),get()) }
    single<PlaylistDataRepository> { PlaylistDataRepositoryImpl(get(),get(),get(),get(named(IO_DISPATCHER))) }
    single<FeedRepository> { FeedRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(),get()) }
}