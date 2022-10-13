package org.listenbrainz.android.data.di.brainzplayer

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.presentation.features.brainzplayer.services.BrainzPlayerServiceConnection
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesServiceConnection( @ApplicationContext context: Context
    ) = BrainzPlayerServiceConnection(context)

    @Singleton
    @Provides
    fun providesContext(@ApplicationContext context: Context): Context = context
    }