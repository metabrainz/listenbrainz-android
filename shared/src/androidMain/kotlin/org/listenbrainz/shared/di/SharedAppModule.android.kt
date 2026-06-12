package org.listenbrainz.shared.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.listenbrainz.shared.provideRemotePlaybackHandler
import org.listenbrainz.shared.repository.remoteplayer.AndroidRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.YouTubeApiService

actual val platformModule = module {

    // Remote Playback Handler
    single<RemotePlaybackHandler> {
        provideRemotePlaybackHandler(androidContext(), youTubeApiService = get<YouTubeApiService>())
    }
}