package org.listenbrainz.android.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.brainzplayer.ArtistRepository
import org.listenbrainz.android.repository.brainzplayer.ArtistRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtistRepositoryModule {
    @Binds
    abstract fun bindsArtistRepository(repository: ArtistRepositoryImpl?) : ArtistRepository?
}