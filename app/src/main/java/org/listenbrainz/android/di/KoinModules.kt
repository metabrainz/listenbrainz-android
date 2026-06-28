package org.listenbrainz.android.di

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.work.WorkManager
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepositoryImpl
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.feed.FeedRepositoryImpl
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManagerImpl
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.repository.user.UserRepositoryImpl
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.repository.yim.YimRepositoryImpl
import org.listenbrainz.android.repository.yim23.Yim23Repository
import org.listenbrainz.android.repository.yim23.Yim23RepositoryImpl
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.android.service.FeedServiceKtor
import org.listenbrainz.android.service.FeedServiceKtorImpl
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
import org.listenbrainz.android.service.Yim23Service
import org.listenbrainz.android.service.YimService
import org.listenbrainz.android.service.createGithubAppUpdatesService
import org.listenbrainz.android.service.createYim23Service
import org.listenbrainz.android.service.createYimService
import org.listenbrainz.android.util.AppStringProvider
import org.listenbrainz.shared.util.Constants.GITHUB_API_BASE_URL
import org.listenbrainz.shared.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.shared.util.Constants.LISTENBRAINZ_BETA_API_BASE_URL
import org.listenbrainz.android.util.LocalMusicSource
import org.listenbrainz.android.util.MusicSource
import org.listenbrainz.android.viewmodel.AboutViewModel
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SearchViewModel
import org.listenbrainz.android.viewmodel.UserViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel
import org.listenbrainz.shared.di.DEFAULT_DISPATCHER
import org.listenbrainz.shared.di.IO_DISPATCHER
import org.listenbrainz.shared.di.platformModule
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.di.sharedDaoModule
import org.listenbrainz.shared.di.sharedDatabaseModule
import org.listenbrainz.shared.di.sharedDispatcherModule
import org.listenbrainz.shared.di.sharedNetworkServiceModule
import org.listenbrainz.shared.di.sharedRepositoryModule
import org.listenbrainz.shared.di.sharedViewModelModule
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.AppPreferencesImpl
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.StringProvider


private val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

private fun createBaseHttpClient(
    context: Context,
    appPreferences: AppPreferences? = null,
    baseUrl: String = LISTENBRAINZ_API_BASE_URL,
    additionalConfig: (io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit)? = null
): HttpClient {
    return HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(jsonConfig)
        }

        install(WebSockets)

        install(HttpRedirect) {
            // Allows redirection for POST method requests
            checkHttpMethod = false
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
                    addInterceptor(ChuckerInterceptor(context))
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


val networkModule = module {
    single<HttpClient> {
        createBaseHttpClient(androidContext(), get<AppPreferences>())
    }


    single<YimService> {
        val httpClient = createBaseHttpClient(androidContext(), baseUrl = LISTENBRAINZ_API_BASE_URL)
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createYimService()
    }

    single<Yim23Service> {
        val httpClient = createBaseHttpClient(androidContext(), baseUrl = LISTENBRAINZ_BETA_API_BASE_URL)
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_BETA_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createYim23Service()
    }

    single<FeedServiceKtor> { FeedServiceKtorImpl(get()) }

    single<GithubAppUpdatesService> {
        val httpClient = HttpClient(OkHttp) {
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
                        addInterceptor(ChuckerInterceptor(androidContext()))
                    }
                }
            }

            defaultRequest {
                url(GITHUB_API_BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }
        Ktorfit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createGithubAppUpdatesService()
    }

    single<GithubUpdatesDownloadService> {
        GithubUpdatesDownloadService(get())
    }
}

val appModule = module {
    single { jsonConfig }

    single<AppPreferences> { AppPreferencesImpl(androidContext()) }

    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    single<ListenServiceManager> {
        ListenServiceManagerImpl(get(), get(), androidContext())
    }

    single<BrainzPlayerServiceConnection> {
        BrainzPlayerServiceConnection(androidContext(), get(), get())
    }

    single<BuildInfo>{
        BuildInfo(
            applicationId = BuildConfig.APPLICATION_ID,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            buildType = BuildConfig.BUILD_TYPE
        )
    }
    single<StringProvider>{
        AppStringProvider()
    }
}

val repositoryModule = module {

    // API Repositories
    single<FeedRepository> { FeedRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<YimRepository> { YimRepositoryImpl(get()) }
    single<Yim23Repository> { Yim23RepositoryImpl(get()) }
    single<AppUpdatesRepository> { AppUpdatesRepositoryImpl(get(), get(), get(named(IO_DISPATCHER))) }

}

// Service Module for BrainzPlayer
val playerModule = module {
    single<AudioAttributes> {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    single<ExoPlayer> {
        ExoPlayer.Builder(androidContext()).build().apply {
            setAudioAttributes(get<AudioAttributes>(), true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    single<MusicSource<MediaMetadataCompat>> {
        LocalMusicSource(get(), get(), get(), get())
    }

    single<LocalMusicSource> {
        LocalMusicSource(get(), get(), get(), get())
    }
}

val viewModelModule = module {
    viewModel { DashBoardViewModel(get(), get(), get(), get(named(IO_DISPATCHER)),get()) }
    viewModel { AppUpdatesViewModel(get(), get(), get()) }
    viewModel { FeedViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { YimViewModel(get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { Yim23ViewModel(get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { SearchViewModel(get(),get(),get(),get(),get(), get(named(IO_DISPATCHER)),get(named(DEFAULT_DISPATCHER))) }
    viewModel { BrainzPlayerViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { AboutViewModel() }
}

val appModules = listOf(
    sharedDispatcherModule,
    networkModule,
    appModule,
    repositoryModule,
    playerModule,
    viewModelModule,
    sharedViewModelModule,
    sharedNetworkServiceModule,
    sharedRepositoryModule,
    platformModule,
    sharedDatabaseModule,
    sharedDaoModule
)
