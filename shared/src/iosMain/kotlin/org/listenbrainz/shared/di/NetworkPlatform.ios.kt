package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import org.listenbrainz.shared.repository.PlatformContext

actual fun getPlatformNetworkEngine(context: PlatformContext): HttpClientEngineFactory<*> = Darwin