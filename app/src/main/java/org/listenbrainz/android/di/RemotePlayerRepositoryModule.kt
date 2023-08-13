package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.remoteplayer.RemotePlayerRepository
import org.listenbrainz.android.repository.remoteplayer.RemotePlayerRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RemotePlayerRepositoryModule {
    
    @Binds
    abstract fun bindsRemotePlayerRepository(repository: RemotePlayerRepositoryImpl?): RemotePlayerRepository?
    
}