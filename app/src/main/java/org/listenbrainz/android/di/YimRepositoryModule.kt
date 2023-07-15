package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.repository.yim.YimRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class YimRepositoryModule {
    @Binds
    abstract fun bindsYimRepository(repository: YimRepositoryImpl?): YimRepository?
}