package org.listenbrainz.android.di

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.application.App
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepositoryImpl
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.service.createGithubAppUpdatesService
import org.listenbrainz.android.util.Constants.GITHUB_API_BASE_URL
import org.listenbrainz.android.util.Log
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppUpdatesServiceModule {

    @Binds
    abstract fun bindsAppUpdatesRepository(appUpdatesRepositoryImpl: AppUpdatesRepositoryImpl): AppUpdatesRepository

    companion object {
        private val jsonConfig = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        private fun createHttpClient(): HttpClient {
            return HttpClient(OkHttp) {
                expectSuccess = true

                install(ContentNegotiation) {
                    json(jsonConfig)
                }

                if (BuildConfig.DEBUG) {
                    install(Logging) {
                        logger = object : Logger {
                            override fun log(message: String) {
                                Log.d("Ktor: $message")
                            }
                        }
                        level = LogLevel.ALL
                    }

                    engine {
                        config {
                            addInterceptor(ChuckerInterceptor(App.context))
                        }
                    }
                }

                defaultRequest {
                    url(GITHUB_API_BASE_URL)
                    contentType(ContentType.Application.Json)
                }
            }
        }

        @Provides
        @Singleton
        fun providesAppUpdatesService(): GithubAppUpdatesService {
            val httpClient = createHttpClient()
            return Ktorfit.Builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .httpClient(httpClient)
                .build()
                .createGithubAppUpdatesService()
        }

        @Provides
        @Singleton
        fun providesGithubAppUpdateDownloadService(context: Application): GithubUpdatesDownloadService {
            return GithubUpdatesDownloadService(context)
        }
    }
}