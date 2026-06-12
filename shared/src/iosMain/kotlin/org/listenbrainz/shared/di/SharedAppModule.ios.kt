package org.listenbrainz.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.listenbrainz.shared.provideRemotePlaybackHandler
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.YouTubeApiService

actual val platformModule = module {

    // Remote Playback Handler
    single<RemotePlaybackHandler> {
        provideRemotePlaybackHandler(PlatformContext(), youTubeApiService = get<YouTubeApiService>())
    }
}