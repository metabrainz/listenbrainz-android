package org.listenbrainz.android.di

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.Binds
import dagger.Module
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.repository.ListensRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ListensRepositoryModule {
    @Binds
    abstract fun bindsListensRepository(repository: ListensRepositoryImpl?): ListensRepository?
}