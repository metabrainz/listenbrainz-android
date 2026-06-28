package org.listenbrainz.shared.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.service.AlbumService
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.repository.getAppPackageName
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.service.createListensService
import org.listenbrainz.shared.service.createUserService
import org.listenbrainz.shared.service.createYouTubeApiService
import org.listenbrainz.shared.util.Constants
import org.listenbrainz.shared.util.PlatformUtils
import org.listenbrainz.shared.service.ArtistService
import org.listenbrainz.shared.service.CBService
import org.listenbrainz.shared.service.MBService
import org.listenbrainz.shared.service.PlaylistService
import org.listenbrainz.shared.service.createAlbumService
import org.listenbrainz.shared.service.SocialService
import org.listenbrainz.shared.service.createArtistService
import org.listenbrainz.shared.service.createMBService
import org.listenbrainz.shared.service.createCBService
import org.listenbrainz.shared.service.createSocialService
import org.listenbrainz.shared.service.createPlaylistService
import org.listenbrainz.shared.util.Constants.CB_BASE_URL
import org.listenbrainz.shared.util.Constants.LB_BASE_URL
import org.listenbrainz.shared.util.Constants.LISTENBRAINZ_API_BASE_URL
import org.listenbrainz.shared.util.Constants.MB_BASE_URL
import kotlin.time.Duration.Companion.milliseconds
import org.listenbrainz.shared.service.BlogService
import org.listenbrainz.shared.service.createBlogService
import org.listenbrainz.shared.util.Log

// Qualifier names for dispatchers
const val DEFAULT_DISPATCHER = "DefaultDispatcher"
const val IO_DISPATCHER = "IoDispatcher"
const val MAIN_DISPATCHER = "MainDispatcher"

const val SHARED_HTTP_CLIENT = "SharedHttpClient"

private val sharedJsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    encodeDefaults = true
    explicitNulls = false
}

private fun createSharedBaseHttpClient(
    appPreference: AppPreferences,
    platformContext: PlatformContext
): HttpClient{
    return HttpClient(getPlatformNetworkEngine()){
        expectSuccess = true

        engine {
            configPlatformEngine(this,platformContext)
        }

        install(ContentNegotiation){
            json(sharedJsonConfig)
        }
        install(WebSockets)

        install(HttpRedirect){
            // Allows redirection for POST method requests
            checkHttpMethod = false
        }

        if(BuildKonfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            appPreference.let { prefs->
                runBlocking {
                    runCatching {
                        withTimeout(3000.milliseconds){
                            val accessToken = prefs.lbAccessToken.get()
                            if(accessToken.isNotEmpty()){
                                header("Authorization","Token $accessToken")
                            }
                        }
                    }.getOrElse {
                        println("Error loading access token: ${it.message}")
                    }
                }
            }
        }
    }
}

val sharedNetworkServiceModule = module {
    single<HttpClient>(named(SHARED_HTTP_CLIENT)) {
        createSharedBaseHttpClient(get<AppPreferences>(),get())
    }

    single<BlogService> {
        Ktorfit.Builder()
            .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createBlogService()
    }

    single<YouTubeApiService> {
        val platformContext = get<PlatformContext>()
        val client = get<HttpClient>(named(SHARED_HTTP_CLIENT)).config {
            defaultRequest {
                url("https://www.googleapis.com/")
                val sha1 = PlatformUtils.getSHA1(platformContext, getAppPackageName(platformContext))
                if(!(sha1.isNullOrBlank())){
                    header("X-Android-Package", getAppPackageName(platformContext))
                    header("X-Android-Cert", sha1)
                }
            }
        }
        Ktorfit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .httpClient(client)
            .build()
            .createYouTubeApiService()
    }


    single<ArtistService> {
        Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createArtistService()
    }

    single<MBService>{
        val client = get<HttpClient>(named(SHARED_HTTP_CLIENT)).config {
            defaultRequest {
                url(MB_BASE_URL)
                header("user-agent","ListenBrainz Android")
                header("accept","application/json")
            }
        }
        Ktorfit.Builder()
            .baseUrl(MB_BASE_URL)
            .httpClient(client)
            .build()
            .createMBService()
    }

    single<CBService> {
        Ktorfit.Builder()
            .baseUrl(CB_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createCBService()
    }

    single<AlbumService>{
        Ktorfit.Builder()
            .baseUrl(LB_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createAlbumService()
    }


    single<ListensService> {
        Ktorfit.Builder()
            .baseUrl(Constants.LISTENBRAINZ_API_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createListensService()
    }
    single<UserService> {
        Ktorfit.Builder()
            .baseUrl(Constants.LISTENBRAINZ_API_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createUserService()
    }

    single<SocialService> {
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createSocialService()
    }

    single<PlaylistService>{
        val client = get<HttpClient>(named(SHARED_HTTP_CLIENT)).config {
            install(HttpTimeout){
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
        Ktorfit.Builder()
            .baseUrl(LISTENBRAINZ_API_BASE_URL)
            .httpClient(client)
            .build()
            .createPlaylistService()
    }

}

val sharedDispatcherModule = module {
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
}