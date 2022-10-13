package org.listenbrainz.android.data.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.repository.AlbumRepository
import org.listenbrainz.android.data.repository.AlbumRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AlbumRepositoryModule {
    @Binds
    abstract fun bindsAlbumRepository(repository: AlbumRepositoryImpl?) : AlbumRepository?
}