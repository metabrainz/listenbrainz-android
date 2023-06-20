package org.listenbrainz.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesServiceConnection(@ApplicationContext context: Context, appPreferences: AppPreferences, listensRepository: ListensRepository) = BrainzPlayerServiceConnection(context, appPreferences, listensRepository)

    @Singleton
    @Provides
    fun providesContext(@ApplicationContext context: Context): Context = context
    
    @Singleton
    @Provides
    fun providesAppPreferences(@ApplicationContext context: Context) : AppPreferences = AppPreferencesImpl(context)
}