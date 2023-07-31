package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.feed.FeedRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedRepositoryModule {
    
    @Binds
    abstract fun bindsFeedRepository(repository: FeedRepositoryImpl?): FeedRepository?
}