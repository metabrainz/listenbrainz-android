package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandlerImpl
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManagerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RemotePlayerRepositoryModule {
    
    @Binds
    abstract fun bindsRemotePlayerRepository(repository: RemotePlaybackHandlerImpl?): RemotePlaybackHandler?
    
    
    @Binds
    abstract fun bindsListenServiceManager(listenServiceManager: ListenServiceManagerImpl): ListenServiceManager
}