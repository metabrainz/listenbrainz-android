package org.listenbrainz.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.listenbrainz.android.di.AppModule
import org.listenbrainz.android.repository.AppPreferences
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
    fun providesServiceConnection( @ApplicationContext context: Context
    ) = BrainzPlayerServiceConnection(context)
    
    @Singleton
    @Provides
    fun providesContext(@ApplicationContext context: Context): Context = context
    
    @Singleton
    @Provides
    fun providesAppPreferences() : AppPreferences = MockAppPreferences()
    
}