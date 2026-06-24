package org.listenbrainz.shared.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.config
import io.ktor.client.engine.okhttp.OkHttp
import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.repository.PlatformContext


actual fun getPlatformNetworkEngine(context: PlatformContext): HttpClientEngineFactory<*> {
    return if(BuildKonfig.DEBUG){
        OkHttp.config {
            addInterceptor(ChuckerInterceptor(context))
        }
    }
    else{
        OkHttp
    }
}