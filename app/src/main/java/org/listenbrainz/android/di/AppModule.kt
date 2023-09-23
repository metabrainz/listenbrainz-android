package org.listenbrainz.android.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    fun providesAppPreferences(@ApplicationContext context: Context) : AppPreferences =
        AppPreferencesImpl(context)
    
    @Provides
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
    
    @Singleton
    @Provides
    fun providesServiceConnection(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences,
        workManager: WorkManager
    ) = BrainzPlayerServiceConnection(context, appPreferences, workManager)
    
}