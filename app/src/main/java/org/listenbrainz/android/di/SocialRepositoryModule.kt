package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.repository.SocialRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class SocialRepositoryModule {
    
    @Binds
    abstract fun bindsSocialRepository(repository: SocialRepositoryImpl?): SocialRepository?
}