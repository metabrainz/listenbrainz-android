package org.listenbrainz.android.data.di.brainzplayer

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.dao.AlbumDao
import org.listenbrainz.android.data.dao.ArtistDao
import org.listenbrainz.android.data.dao.PlaylistDao
import org.listenbrainz.android.data.dao.SongDao


@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun provideSongDao(
        database: BrainzPlayerDatabase
    ):SongDao = database.songDao()

    @Provides
    fun provideAlbumDao(
        database: BrainzPlayerDatabase
    ):AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(
        database: BrainzPlayerDatabase
    ): ArtistDao = database.artistDao()

    @Provides
    fun providePlaylistDao(
        database: BrainzPlayerDatabase
    ): PlaylistDao = database.playlistDao()
}