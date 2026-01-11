package org.listenbrainz.android.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.application.App
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.service.AlbumService
import org.listenbrainz.android.service.ArtistService
import org.listenbrainz.android.service.BlogService
import org.listenbrainz.android.service.CBService
import org.listenbrainz.android.service.FeedServiceKtor
import org.listenbrainz.android.service.FeedServiceKtorImpl
import org.listenbrainz.android.service.ListensService
import org.listenbrainz.android.service.MBService
import org.listenbrainz.android.service.PlaylistService
import org.listenbrainz.android.service.SocialService
import org.listenbrainz.android.service.UserService
import org.listenbrainz.android.service.Yim23Service
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.service.YouTubeApiService
import org.listenbrainz.android.service.createAlbumService
import org.listenbrainz.android.service.createArtistService
import org.listenbrainz.android.service.createBlogService
import org.listenbrainz.android.service.createCBService
import org.listenbrainz.android.service.createListensService
import org.listenbrainz.android.service.createMBService
import org.listenbrainz.android.service.createPlaylistService
import org.listenbrainz.android.service.createSocialService
import org.listenbrainz.android.service.createUserService
import org.listenbrainz.android.service.createYim23Service
import org.listenbrainz.android.service.createYimService
import org.listenbrainz.android.service.createYouTubeApiService
import org.listenbrainz.android.util.Constants.CB_BASE_URL
import org.listenbrainz.android.util.Constants.LB_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_BETA_API_BASE_URL
import org.listenbrainz.android.util.Constants.MB_BASE_URL
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Utils
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    private fun createBaseHttpClient(
        appPreferences: AppPreferences? = null,
        baseUrl: String = LISTENBRAINZ_API_BASE_URL,
        additionalConfig: (io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit)? = null
    ): HttpClient {
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
                url(baseUrl)
                contentType(ContentType.Application.Json)

                appPreferences?.let { prefs ->
                    runBlocking {
                        runCatching {
                            withTimeout(3000) {
                                val accessToken = prefs.lbAccessToken.get()
                                if (accessToken.isNotEmpty()) {
                                    header("Authorization", "Token $accessToken")
                                }
                            }
                        }.getOrElse {
                            Log.d("Error loading access token: ${it.message}")
                        }
                    }
                }
            }

            additionalConfig?.invoke(this)
        }
    }

    @Singleton
    @Provides
    fun providesHttpClient(appPreferences: AppPreferences): HttpClient {
        return createBaseHttpClient(appPreferences)
    }

    @Singleton
    @Provides
    fun providesBlogService(): BlogService {
        val httpClient = createBaseHttpClient(baseUrl = "https://public-api.wordpress.com/rest/v1.1/sites/")
        return Ktorfit.Builder()
            .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
            .httpClient(httpClient)
            .build()
            .createBlogService()
    }

    @Singleton
    @Provides
    fun providesListensService(appPreferences: AppPreferences): ListensService {
        val httpClient = createBaseHttpClient(appPreferences)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createListensService()
    }

    @Singleton
    @Provides
    fun providesSocialService(appPreferences: AppPreferences): SocialService {
        val httpClient = createBaseHttpClient(appPreferences)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createSocialService()
    }

    @Singleton
    @Provides
    fun providesUserService(appPreferences: AppPreferences): UserService {
        val httpClient = createBaseHttpClient(appPreferences)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createUserService()
    }

    @Singleton
    @Provides
    fun providesPlaylistService(appPreferences: AppPreferences): PlaylistService {
        val httpClient = createBaseHttpClient(appPreferences)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createPlaylistService()
    }

    @Singleton
    @Provides
    fun providesArtistService(): ArtistService {
        val httpClient = createBaseHttpClient(baseUrl = LB_BASE_URL)
        return Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createArtistService()
    }

    @Singleton
    @Provides
    fun providesMBService(): MBService {
        val httpClient = HttpClient(OkHttp) {
            expectSuccess = false

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
                url(MB_BASE_URL)
                header("user-agent", "ListenBrainz Android")
                header("accept", "application/json")
            }
        }
        return Ktorfit.Builder()
            .baseUrl(MB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createMBService()
    }

    @Singleton
    @Provides
    fun providesCBService(): CBService {
        val httpClient = createBaseHttpClient(baseUrl = CB_BASE_URL)
        return Ktorfit.Builder()
            .baseUrl(CB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createCBService()
    }

    @Singleton
    @Provides
    fun providesAlbumService(): AlbumService {
        val httpClient = createBaseHttpClient(baseUrl = LB_BASE_URL)
        return Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createAlbumService()
    }

    @Singleton
    @Provides
    fun providesYoutubeApiService(@ApplicationContext context: Context): YouTubeApiService {
        val httpClient = HttpClient(OkHttp) {
            expectSuccess = false

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
                url("https://www.googleapis.com/")
                header("X-Android-Package", context.packageName)
                header("X-Android-Cert", Utils.getSHA1(context, context.packageName) ?: "")
            }
        }
        return Ktorfit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .httpClient(httpClient)
            .build()
            .createYouTubeApiService()
    }

    @Singleton
    @Provides
    fun providesYimService(): YimService {
        val httpClient = createBaseHttpClient(baseUrl = LISTENBRAINZ_API_BASE_URL)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createYimService()
    }

    @Singleton
    @Provides
    fun providesYim23Service(): Yim23Service {
        val httpClient = createBaseHttpClient(baseUrl = LISTENBRAINZ_BETA_API_BASE_URL)
        return Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_BETA_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createYim23Service()
    }

    @Singleton
    @Provides
    fun providesFeedServiceKtor(httpClient: HttpClient): FeedServiceKtor {
        return FeedServiceKtorImpl(httpClient)
    }
}