package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.playlists.PlaylistDataRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaylistRepositoryModule {

    @Binds
    abstract fun bindsPlaylistRepository(repository: PlaylistDataRepositoryImpl): PlaylistDataRepository
}