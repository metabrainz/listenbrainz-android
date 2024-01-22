package org.listenbrainz.android.di.brainzplayer

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.model.dao.AlbumDao
import org.listenbrainz.android.model.dao.ArtistDao
import org.listenbrainz.android.model.dao.PendingListensDao
import org.listenbrainz.android.model.dao.PlaylistDao
import org.listenbrainz.android.model.dao.SongDao


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
    
    @Provides
    fun providesPendingListensDao(
        database: ListensSubmissionDatabase
    ): PendingListensDao = database.pendingListensDao()
}