package org.listenbrainz.android.di.brainzplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.brainzplayer.BPArtistRepository
import org.listenbrainz.android.repository.brainzplayer.BPArtistRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class BPArtistRepositoryModule {
    @Binds
    abstract fun bindsBPArtistRepository(repository: BPArtistRepositoryImpl) : BPArtistRepository
}