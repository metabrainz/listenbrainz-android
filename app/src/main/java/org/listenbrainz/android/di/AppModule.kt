package org.listenbrainz.android.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManagerImpl
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

    @Singleton
    @Provides
    fun providesListenServiceManager(
        workManager: WorkManager,
        appPreferences: AppPreferences,
        @ApplicationContext context: Context
    ): ListenServiceManager =
        ListenServiceManagerImpl(workManager, appPreferences,  context)
    
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