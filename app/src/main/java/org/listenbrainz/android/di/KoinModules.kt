package org.listenbrainz.android.di

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import androidx.room.Room
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
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.android.BuildConfig
import org.listenbrainz.android.di.brainzplayer.BrainzPlayerDatabase
import org.listenbrainz.android.di.brainzplayer.ListensSubmissionDatabase
import org.listenbrainz.android.di.brainzplayer.Migrations
import org.listenbrainz.android.model.dao.AlbumDao
import org.listenbrainz.android.model.dao.ArtistDao
import org.listenbrainz.android.model.dao.PendingListensDao
import org.listenbrainz.android.model.dao.PlaylistDao
import org.listenbrainz.android.model.dao.SongDao
import org.listenbrainz.android.repository.album.AlbumRepository
import org.listenbrainz.android.repository.album.AlbumRepositoryImpl
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepositoryImpl
import org.listenbrainz.android.repository.artist.ArtistRepository
import org.listenbrainz.android.repository.artist.ArtistRepositoryImpl
import org.listenbrainz.android.repository.blog.BlogRepository
import org.listenbrainz.android.repository.blog.BlogRepositoryImpl
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepository
import org.listenbrainz.android.repository.brainzplayer.BPAlbumRepositoryImpl
import org.listenbrainz.android.repository.brainzplayer.BPArtistRepository
import org.listenbrainz.android.repository.brainzplayer.BPArtistRepositoryImpl
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepository
import org.listenbrainz.android.repository.brainzplayer.PlaylistRepositoryImpl
import org.listenbrainz.android.repository.brainzplayer.SongRepository
import org.listenbrainz.android.repository.brainzplayer.SongRepositoryImpl
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.feed.FeedRepositoryImpl
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.listens.ListensRepositoryImpl
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManager
import org.listenbrainz.android.repository.listenservicemanager.ListenServiceManagerImpl
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.playlists.PlaylistDataRepositoryImpl
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.AppPreferencesImpl
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandlerImpl
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.social.SocialRepositoryImpl
import org.listenbrainz.android.repository.socket.SocketRepository
import org.listenbrainz.android.repository.socket.SocketRepositoryImpl
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.repository.user.UserRepositoryImpl
import org.listenbrainz.android.repository.yim.YimRepository
import org.listenbrainz.android.repository.yim.YimRepositoryImpl
import org.listenbrainz.android.repository.yim23.Yim23Repository
import org.listenbrainz.android.repository.yim23.Yim23RepositoryImpl
import org.listenbrainz.android.service.AlbumService
import org.listenbrainz.android.service.ArtistService
import org.listenbrainz.android.service.BlogService
import org.listenbrainz.android.service.BrainzPlayerServiceConnection
import org.listenbrainz.android.service.CBService
import org.listenbrainz.android.service.FeedServiceKtor
import org.listenbrainz.android.service.FeedServiceKtorImpl
import org.listenbrainz.android.service.GithubAppUpdatesService
import org.listenbrainz.android.service.GithubUpdatesDownloadService
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
import org.listenbrainz.android.service.createGithubAppUpdatesService
import org.listenbrainz.android.service.createListensService
import org.listenbrainz.android.service.createMBService
import org.listenbrainz.android.service.createPlaylistService
import org.listenbrainz.android.service.createSocialService
import org.listenbrainz.android.service.createUserService
import org.listenbrainz.android.service.createYim23Service
import org.listenbrainz.android.service.createYimService
import org.listenbrainz.android.service.createYouTubeApiService
import org.listenbrainz.android.util.Constants.CB_BASE_URL
import org.listenbrainz.android.util.Constants.GITHUB_API_BASE_URL
import org.listenbrainz.android.util.Constants.LB_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.android.util.Constants.LISTENBRAINZ_BETA_API_BASE_URL
import org.listenbrainz.android.util.Constants.MB_BASE_URL
import org.listenbrainz.android.util.LocalMusicSource
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.MusicSource
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.viewmodel.AboutViewModel
import org.listenbrainz.android.viewmodel.AlbumViewModel
import org.listenbrainz.android.viewmodel.AppUpdatesViewModel
import org.listenbrainz.android.viewmodel.ArtistViewModel
import org.listenbrainz.android.viewmodel.BPAlbumViewModel
import org.listenbrainz.android.viewmodel.BPArtistViewModel
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.android.viewmodel.FeaturesViewModel
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.ListeningNowViewModel
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.LoginViewModel
import org.listenbrainz.android.viewmodel.NewsListViewModel
import org.listenbrainz.android.viewmodel.PlaylistDataViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel
import org.listenbrainz.android.viewmodel.SearchViewModel
import org.listenbrainz.android.viewmodel.SettingsViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.SongViewModel
import org.listenbrainz.android.viewmodel.UserViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

// Qualifier names for dispatchers
const val DEFAULT_DISPATCHER = "DefaultDispatcher"
const val IO_DISPATCHER = "IoDispatcher"
const val MAIN_DISPATCHER = "MainDispatcher"

private val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    encodeDefaults = true
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

val dispatcherModule = module {
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
}

val databaseModule = module {
    single<BrainzPlayerDatabase> {
        Room.databaseBuilder(
            androidContext(),
            BrainzPlayerDatabase::class.java,
            "brainzplayer_database"
        )
            .addMigrations(Migrations.MIGRATION_1_2)
            .build()
    }

    single<ListensSubmissionDatabase> {
        Room.databaseBuilder(
            androidContext(),
            ListensSubmissionDatabase::class.java,
            "listens_scrobble_database"
        )
            .build()
    }
}

val daoModule = module {
    single<SongDao> { get<BrainzPlayerDatabase>().songDao() }
    single<AlbumDao> { get<BrainzPlayerDatabase>().albumDao() }
    single<ArtistDao> { get<BrainzPlayerDatabase>().artistDao() }
    single<PlaylistDao> { get<BrainzPlayerDatabase>().playlistDao() }
    single<PendingListensDao> { get<ListensSubmissionDatabase>().pendingListensDao() }
}

val networkModule = module {
    single<HttpClient> {
        createBaseHttpClient(androidContext(), get<AppPreferences>())
    }

    single<BlogService> {
        val httpClient = createBaseHttpClient(
            androidContext(),
            baseUrl = "https://public-api.wordpress.com/rest/v1.1/sites/"
        )
        Ktorfit.Builder()
            .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
            .httpClient(httpClient)
            .build()
            .createBlogService()
    }

    single<ListensService> {
        val httpClient = createBaseHttpClient(androidContext(), get<AppPreferences>())
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createListensService()
    }

    single<SocialService> {
        val httpClient = createBaseHttpClient(androidContext(), get<AppPreferences>())
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createSocialService()
    }

    single<UserService> {
        val httpClient = createBaseHttpClient(androidContext(), get<AppPreferences>())
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createUserService()
    }

    single<PlaylistService> {
        val httpClient = createBaseHttpClient(androidContext(), get<AppPreferences>())
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createPlaylistService()
    }

    single<ArtistService> {
        val httpClient = createBaseHttpClient(androidContext(), baseUrl = LB_BASE_URL)
        Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createArtistService()
    }

    single<MBService> {
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
                        addInterceptor(ChuckerInterceptor(androidContext()))
                    }
                }
            }

            defaultRequest {
                url(MB_BASE_URL)
                header("user-agent", "ListenBrainz Android")
                header("accept", "application/json")
            }
        }
        Ktorfit.Builder()
            .baseUrl(MB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createMBService()
    }

    single<CBService> {
        val httpClient = createBaseHttpClient(androidContext(), baseUrl = CB_BASE_URL)
        Ktorfit.Builder()
            .baseUrl(CB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createCBService()
    }

    single<AlbumService> {
        val httpClient = createBaseHttpClient(androidContext(), baseUrl = LB_BASE_URL)
        Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(httpClient)
            .build()
            .createAlbumService()
    }

    single<YouTubeApiService> {
        val context = androidContext()
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
                        addInterceptor(ChuckerInterceptor(context))
                    }
                }
            }

            defaultRequest {
                url("https://www.googleapis.com/")
                header("X-Android-Package", context.packageName)
                header("X-Android-Cert", Utils.getSHA1(context, context.packageName) ?: "")
            }
        }
        Ktorfit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .httpClient(httpClient)
            .build()
            .createYouTubeApiService()
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
    single<AppPreferences> { AppPreferencesImpl(androidContext()) }

    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    single<ListenServiceManager> {
        ListenServiceManagerImpl(get(), get(), androidContext())
    }

    single<BrainzPlayerServiceConnection> {
        BrainzPlayerServiceConnection(androidContext(), get(), get())
    }
}

val repositoryModule = module {
    // BrainzPlayer Repositories
    single<SongRepository> { SongRepositoryImpl(get()) }
    single<BPAlbumRepository> { BPAlbumRepositoryImpl(get()) }
    single<BPArtistRepository> { BPArtistRepositoryImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    single<SocketRepository> { SocketRepositoryImpl() }

    // API Repositories
    single<FeedRepository> { FeedRepositoryImpl(get()) }
    single<SocialRepository> { SocialRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ListensRepository> { ListensRepositoryImpl(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    single<PlaylistDataRepository> { PlaylistDataRepositoryImpl(get(), get(), get(), get(named(IO_DISPATCHER))) }
    single<ArtistRepository> { ArtistRepositoryImpl(get(), get(), get()) }
    single<AlbumRepository> { AlbumRepositoryImpl(get(), get(), get()) }
    single<BlogRepository> { BlogRepositoryImpl(get()) }
    single<YimRepository> { YimRepositoryImpl(get()) }
    single<Yim23Repository> { Yim23RepositoryImpl(get()) }
    single<AppUpdatesRepository> { AppUpdatesRepositoryImpl(get(), get(), get(named(IO_DISPATCHER))) }

    // Remote Playback Handler
    single<RemotePlaybackHandler> { RemotePlaybackHandlerImpl(androidContext(), get()) }
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
    viewModel { DashBoardViewModel(get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { AppUpdatesViewModel(get(), get(), get()) }
    viewModel { FeedViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { ListensViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { SocialViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { UserViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { PlaylistDataViewModel(get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { ListeningNowViewModel(get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { YimViewModel(get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { Yim23ViewModel(get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { NewsListViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { SearchViewModel(get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { BrainzPlayerViewModel(get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    viewModel { PlaylistViewModel(get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER))) }
    viewModel { BPAlbumViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { BPArtistViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { SongViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { ArtistViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { AlbumViewModel(get(), get(named(IO_DISPATCHER))) }
    viewModel { SettingsViewModel(get()) }
    viewModel { FeaturesViewModel(get()) }
    viewModel { AboutViewModel() }
    viewModel { LoginViewModel() }
}

val appModules = listOf(
    dispatcherModule,
    databaseModule,
    daoModule,
    networkModule,
    appModule,
    repositoryModule,
    playerModule,
    viewModelModule
)
