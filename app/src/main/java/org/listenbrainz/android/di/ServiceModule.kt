package org.listenbrainz.android.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
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
import org.listenbrainz.android.util.Constants.CB_BASE_URL
import org.listenbrainz.android.util.Constants.LB_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_BETA_API_BASE_URL
import org.listenbrainz.android.util.Constants.MB_BASE_URL
import org.listenbrainz.android.util.HeaderInterceptor
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Utils
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    val contentType = "application/json".toMediaType()
    
    private val okHttpClient by lazy {
        OkHttpClient
            .Builder()
            .addInterceptor(ChuckerInterceptor(App.context))
            .build()
    }

    private fun constructRetrofit(appPreferences: AppPreferences): Retrofit =
        Retrofit.Builder()
            .client(
                okHttpClient
                    .newBuilder()
                    .addInterceptor(HeaderInterceptor(appPreferences))
                    //.addInterceptor (HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                    .build()
            )
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

    @get:Singleton
    @get:Provides
    val blogService: BlogService = Retrofit.Builder()
        .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build().create(BlogService::class.java)

    @Singleton
    @Provides
    fun providesListensService(appPreferences: AppPreferences): ListensService =
        constructRetrofit(appPreferences)
            .create(ListensService::class.java)


    @Singleton
    @Provides
    fun providesSocialService(appPreferences: AppPreferences): SocialService =
        constructRetrofit(appPreferences)
            .create(SocialService::class.java)

    @Singleton
    @Provides
    fun providesUserService(appPreferences: AppPreferences) : UserService =
        constructRetrofit(appPreferences)
        .create(UserService::class.java)

    @Singleton
    @Provides
    fun providesPlaylistService(appPreferences: AppPreferences) : PlaylistService =
        constructRetrofit(appPreferences)
            .create(PlaylistService::class.java)

    @Singleton
    @Provides
    fun providesArtistService(): ArtistService = Retrofit.Builder()
        .baseUrl(LB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build().create(ArtistService::class.java)

    @Singleton
    @Provides
    fun providesMBService(): MBService {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("user-agent", "ListenBrainz Android")
                    .header("accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(MB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MBService::class.java)
    }

    @Singleton
    @Provides
    fun providesCBService(): CBService = Retrofit.Builder()
        .baseUrl(CB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build().create(CBService::class.java)

    @Singleton
    @Provides
    fun providesAlbumService(): AlbumService = Retrofit.Builder()
        .baseUrl(LB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build().create(AlbumService::class.java)

    @Singleton
    @Provides
    fun providesYoutubeApiService(@ApplicationContext context: Context): YouTubeApiService =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .client( OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("X-Android-Package", context.packageName)
                        .addHeader("X-Android-Cert", Utils.getSHA1(context, context.packageName) ?: "")
                        .build()
                    chain.proceed(request)
                }
                .build()
            )
            .build()
            .create(YouTubeApiService::class.java)


    @get:Singleton
    @get:Provides
    val yimService: YimService = Retrofit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(YimService::class.java)

    @get:Singleton
    @get:Provides
    val yim23Service: Yim23Service = Retrofit.Builder()
        //TODO : To be removed when YIM goes live
        .baseUrl(LISTENBRAINZ_BETA_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(Yim23Service::class.java)

    @Singleton
    @Provides
    fun providesHttpClient(appPreferences: AppPreferences): HttpClient {
        return HttpClient(OkHttp) {
            /*
             * RedirectResponseException for 3xx responses.
             * ClientRequestException for 4xx responses.
             * ServerResponseException for 5xx responses.
             */
            expectSuccess = true

            // TODO: Use HttpResponseValidator in future.

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true

                    /*
                     Default values aren't encoded by default in kotlinx.serialization
                     setting this field to true will ensure default encoding
                     source: https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#encoding-defaults
                                            *encodeDefaults = true*
                     */
                })
            }

            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("Ktor_Android: $message")
                        }
                    }
                    level = LogLevel.ALL
                }

                engine {
                    config {
                        //  only helpful for androidMain, expected to not work on other source targets
                        addInterceptor(ChuckerInterceptor(App.context))
                    }
                }
            }

            defaultRequest {
                url(LISTENBRAINZ_API_BASE_URL)

                runBlocking {
                    runCatching {
                        withTimeout(3000) {
                            val accessToken = appPreferences.lbAccessToken.get()
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
    }

    @Singleton
    @Provides
    fun providesFeedServiceKtor(httpClient: HttpClient): FeedServiceKtor {
        return FeedServiceKtorImpl(httpClient)
    }
}