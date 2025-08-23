package org.listenbrainz.android.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.listenbrainz.android.application.App
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepositoryImpl
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.util.Constants.GITHUB_API_BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdatesServiceModule {

    @Binds
    abstract fun bindsAppUpdatesRepository(appUpdatesRepositoryImpl: AppUpdatesRepositoryImpl): AppUpdatesRepository

    companion object {

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
                .addConverterFactory(GsonConverterFactory.create())
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