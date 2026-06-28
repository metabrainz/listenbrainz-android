package org.listenbrainz.shared.di

import org.koin.dsl.module
import org.listenbrainz.shared.provideRemotePlaybackHandler
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.provideSongData
import org.listenbrainz.shared.util.SongsData
import org.listenbrainz.shared.provideLogSubmitter
import org.listenbrainz.shared.provideLogger
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter
import org.listenbrainz.shared.provideAlbumsData
import org.listenbrainz.shared.util.AlbumsData


val platformModule = module {
    single<LogSubmitter>{
        provideLogSubmitter(buildInfo = get<BuildInfo>())
    }

    single<co.touchlab.kermit.Logger> {
        provideLogger(buildInfo = get<BuildInfo>())
    }

    single<RemotePlaybackHandler> {
        provideRemotePlaybackHandler(youTubeApiService = get<YouTubeApiService>())
    }
    single<SongsData>{
        provideSongData()
    }
    single<AlbumsData>{
        provideAlbumsData()
    }
}