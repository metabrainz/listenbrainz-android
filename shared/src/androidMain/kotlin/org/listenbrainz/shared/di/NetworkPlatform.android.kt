package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun getPlatformNetworkEngine(): HttpClientEngineFactory<*>  = OkHttp