package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.ListensRepository
import org.listenbrainz.android.repository.ListensRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ListensRepositoryModule {
    @Binds
    abstract fun bindsListensRepository(repository: ListensRepositoryImpl?): ListensRepository?
}