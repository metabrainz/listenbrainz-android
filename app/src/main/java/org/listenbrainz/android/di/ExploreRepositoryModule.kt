package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.explore.ExploreRepository
import org.listenbrainz.android.repository.explore.ExploreRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreRepositoryModule {

    @Binds
    abstract fun bindsExploreRepository(repository: ExploreRepositoryImpl?): ExploreRepository?
}
