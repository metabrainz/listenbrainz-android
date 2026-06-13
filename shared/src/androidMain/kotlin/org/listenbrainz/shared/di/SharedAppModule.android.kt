package org.listenbrainz.shared.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.provideListensRepositoryImpl
import org.listenbrainz.shared.provideRemotePlaybackHandler
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.AndroidRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService

actual val platformModule = module {

    // Remote Playback Handler
    single<RemotePlaybackHandler> {
        provideRemotePlaybackHandler(androidContext(), youTubeApiService = get<YouTubeApiService>())
    }

    single<ListensRepository> {
        provideListensRepositoryImpl(
            get<ListensService>(),
            get<AppPreferences>(),
            get<UserService>(),
            get<PendingListensDao>(),
            get(
                named(IO_DISPATCHER)
            ),
            androidContext()
        )
    }
}