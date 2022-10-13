package org.listenbrainz.android.data.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.data.repository.ArtistRepository
import org.listenbrainz.android.data.repository.ArtistRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtistRepositoryModule {
    @Binds
    abstract fun bindsArtistRepository(repository: ArtistRepositoryImpl?) : ArtistRepository?
}