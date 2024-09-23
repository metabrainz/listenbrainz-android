package org.listenbrainz.android.di

import android.content.Context

import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.sharedtest.mocks.MockAppPreferences
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class TestAppModule {

    @Singleton
    @Provides
    fun providesServiceConnection(@ApplicationContext context: Context, appPreferences: AppPreferences, workManager: WorkManager): BrainzPlayerServiceConnection {
        return BrainzPlayerServiceConnection(context, appPreferences, workManager)
    }
    
    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
    
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        return WorkManager.getInstance(context)
    }

    @Singleton
    @Provides
    fun providesContext(@ApplicationContext context: Context): Context = context

    @Singleton
    @Provides
    fun providesAppPreferences() : AppPreferences = MockAppPreferences()
    
}
