package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.social.SocialRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class SocialRepositoryModule {
    
    @Binds
    abstract fun bindsSocialRepository(repository: SocialRepositoryImpl?): SocialRepository?
}