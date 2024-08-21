package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.album.AlbumRepository
import org.listenbrainz.android.repository.album.AlbumRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AlbumRepositoryModule {

    @Binds
    abstract fun bindsAlbumRepository (repository: AlbumRepositoryImpl?) : AlbumRepository?
}