package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineFactory
import org.listenbrainz.shared.repository.PlatformContext

expect fun getPlatformNetworkEngine(context: PlatformContext) : HttpClientEngineFactory<*>