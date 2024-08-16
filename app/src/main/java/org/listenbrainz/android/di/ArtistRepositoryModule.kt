package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.artist.ArtistRepository
import org.listenbrainz.android.repository.artist.ArtistRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ArtistRepositoryModule {

    @Binds
    abstract fun bindsArtistRepository (repository: ArtistRepositoryImpl?) : ArtistRepository?
}