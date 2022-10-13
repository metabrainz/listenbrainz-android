package org.listenbrainz.android.data.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.repository.PlaylistRepository
import org.listenbrainz.android.data.repository.PlaylistRepositoryImpl

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class PlaylistRepositoryModule {
        @Binds
        abstract fun bindsPlaylistRepository(repository: PlaylistRepositoryImpl?): PlaylistRepository?
    }