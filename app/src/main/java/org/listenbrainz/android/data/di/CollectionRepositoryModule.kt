package org.listenbrainz.android.data.di

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.Binds
import dagger.Module
import org.listenbrainz.android.data.repository.CollectionRepository
import org.listenbrainz.android.data.repository.CollectionRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class CollectionRepositoryModule {
    @Binds
    abstract fun bindsCollectionRepository(repository: CollectionRepositoryImpl?): CollectionRepository?
}