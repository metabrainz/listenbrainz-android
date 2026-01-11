package org.listenbrainz.android.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepository
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AlbumRepositoryModule {
    @Binds
    abstract fun bindsAlbumRepository(repository: BPAlbumRepositoryImpl) : BPAlbumRepository
}