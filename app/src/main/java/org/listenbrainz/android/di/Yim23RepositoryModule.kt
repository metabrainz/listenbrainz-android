package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import org.listenbrainz.android.repository.yim23.Yim23Repository
import org.listenbrainz.android.repository.yim23.Yim23RepositoryImpl

// TODO: TO BE REMOVED WHEN YIM GOES LIVE
@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class Yim23RepositoryModule {
    @Binds
    abstract fun bindsYim23Repository(repository: Yim23RepositoryImpl?) : Yim23Repository
}