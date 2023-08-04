package org.listenbrainz.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.android.util.CachingInterceptor
import org.listenbrainz.android.util.HeaderInterceptor
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesServiceConnection(@ApplicationContext context: Context, appPreferences: AppPreferences, listensRepository: ListensRepository) =
        BrainzPlayerServiceConnection(context, appPreferences, listensRepository)

    @Provides
    fun providesAppPreferences(@ApplicationContext context: Context) : AppPreferences =
        AppPreferencesImpl(context)
    
    @Provides
    fun providesHeaderInterceptor(appPreferences: AppPreferences): HeaderInterceptor =
        HeaderInterceptor(appPreferences)
    
    @Provides
    fun providesCacheInterceptor(@ApplicationContext context: Context): CachingInterceptor =
        CachingInterceptor(context)
    
    @OkHttpCache
    @Singleton
    @Provides
    fun providesOkHttpCache(@ApplicationContext context: Context) : Cache =
        Cache(context.cacheDir, 5 * 1024 * 1024)    // 5 MB
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OkHttpCache