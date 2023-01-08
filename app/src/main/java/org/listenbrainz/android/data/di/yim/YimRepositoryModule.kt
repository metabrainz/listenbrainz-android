package org.listenbrainz.android.data.di.yim

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.repository.YimRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class YimRepositoryModule {
    @Binds
    abstract fun bindsYimRepository(repository: YimRepositoryImpl?): YimRepository?
}