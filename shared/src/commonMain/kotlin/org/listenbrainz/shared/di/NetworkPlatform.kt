package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineFactory

expect fun getPlatformNetworkEngine() : HttpClientEngineFactory<*>