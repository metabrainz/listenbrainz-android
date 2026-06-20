package org.listenbrainz.shared.di

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.http.HTTP
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.service.AlbumService
import org.listenbrainz.shared.service.ArtistService
import org.listenbrainz.shared.service.CBService
import org.listenbrainz.shared.service.MBService
import org.listenbrainz.shared.service.createAlbumService
import org.listenbrainz.shared.service.createArtistService
import org.listenbrainz.shared.service.createMBService
import org.listenbrainz.shared.service.createCBService
import org.listenbrainz.shared.util.Constants.CB_BASE_URL
import org.listenbrainz.shared.util.Constants.LB_BASE_URL
import org.listenbrainz.shared.util.Constants.MB_BASE_URL
import org.listenbrainz.shared.service.BlogService
import org.listenbrainz.shared.service.createBlogService

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

val sharedNetworkServiceModule = module {
    single<HttpClient>(named(SHARED_HTTP_CLIENT)) {
        HttpClient(getPlatformNetworkEngine()){
            expectSuccess = true

            install(ContentNegotiation){
                json(sharedJsonConfig)
            }
            install(HttpRedirect){
                checkHttpMethod = false
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Ktor: $message")
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    single<BlogService> {
        Ktorfit.Builder()
            .baseUrl("https://public-api.wordpress.com/rest/v1.1/sites/")
            .httpClient(get<HttpClient>(named(SHARED_HTTP_CLIENT)))
            .build()
            .createBlogService()
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

}

val sharedDispatcherModule = module {
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
}