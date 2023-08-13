package org.listenbrainz.android.di

import android.content.Context
import androidx.work.WorkManager
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
    fun providesServiceConnection(
        @ApplicationContext context: Context,
        appPreferences: AppPreferences,
        workManager: WorkManager
    ) = BrainzPlayerServiceConnection(context, appPreferences, workManager)

    @Singleton
    @Provides
    fun providesContext(@ApplicationContext context: Context): Context = context

    @Singleton
    @Provides
    fun providesAppPreferences() : AppPreferences = MockAppPreferences()

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
