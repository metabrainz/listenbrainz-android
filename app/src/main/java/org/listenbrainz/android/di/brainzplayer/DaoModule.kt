package org.listenbrainz.android.di.brainzplayer

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.model.AlbumDao
import org.listenbrainz.android.model.ArtistDao
import org.listenbrainz.android.model.PlaylistDao
import org.listenbrainz.android.model.SongDao


@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideSongDao(
        database: BrainzPlayerDatabase
    ): SongDao = database.songDao()

    @Provides
    fun provideAlbumDao(
        database: BrainzPlayerDatabase
    ): AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(
        database: BrainzPlayerDatabase
    ): ArtistDao = database.artistDao()

    @Provides
    fun providePlaylistDao(
        database: BrainzPlayerDatabase
    ): PlaylistDao = database.playlistDao()
}