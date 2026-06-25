package org.listenbrainz.shared.di

import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import org.listenbrainz.shared.BuildKonfig
import org.listenbrainz.shared.repository.PlatformContext


actual fun getPlatformNetworkEngine(): HttpClientEngineFactory<*> = OkHttp

actual fun configPlatformEngine(config: HttpClientEngineConfig, context: PlatformContext) {
    if(config is OkHttpConfig){
        if(BuildKonfig.DEBUG){
            config.addInterceptor(ChuckerInterceptor(context))
        }
    }
}