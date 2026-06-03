package org.listenbrainz.shared.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun getPlatformNetworkEngine(): HttpClientEngineFactory<*> = Darwin