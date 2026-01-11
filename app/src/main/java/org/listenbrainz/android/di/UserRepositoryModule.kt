package org.listenbrainz.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.repository.user.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    @Binds
    abstract fun bindsUserRepository (repository: UserRepositoryImpl) : UserRepository
}