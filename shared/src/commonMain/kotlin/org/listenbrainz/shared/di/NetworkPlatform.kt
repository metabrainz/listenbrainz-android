package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import org.listenbrainz.shared.repository.PlatformContext

expect fun getPlatformNetworkEngine() : HttpClientEngineFactory<*>

expect fun configPlatformEngine(config: HttpClientEngineConfig,context: PlatformContext)