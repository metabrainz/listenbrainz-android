package org.listenbrainz.android.di

import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.Binds
import dagger.Module
import org.listenbrainz.android.repository.blog.BlogRepository
import org.listenbrainz.android.repository.blog.BlogRepositoryImpl

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class BlogRepositoryModule {
    @Binds
    abstract fun bindsBlogRepository(repository: BlogRepositoryImpl?): BlogRepository?
}