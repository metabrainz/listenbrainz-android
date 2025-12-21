package org.listenbrainz.android.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.listenbrainz.android.application.App
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepositoryImpl
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.util.Constants.GITHUB_API_BASE_URL
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdatesServiceModule {

    @Binds
    abstract fun bindsAppUpdatesRepository(appUpdatesRepositoryImpl: AppUpdatesRepositoryImpl): AppUpdatesRepository

    companion object {
        val json = Json { ignoreUnknownKeys = true }

        private val httpClient by lazy {
            OkHttpClient
                .Builder()
                .addInterceptor(ChuckerInterceptor(App.context))
                .build()
        }

        private fun createRetrofit(): Retrofit {
            return Retrofit.Builder()
                .client(httpClient)
                .baseUrl(GITHUB_API_BASE_URL)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
        }

        @Provides
        @Singleton
        fun providesAppUpdatesService(): GithubAppUpdatesService {
            return createRetrofit().create(GithubAppUpdatesService::class.java)
        }

        @Provides
        @Singleton
        fun providesGithubAppUpdateDownloadService(context: Application): GithubUpdatesDownloadService{
            return GithubUpdatesDownloadService(context)
        }
    }
}