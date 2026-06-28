package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.SongsData
import org.listenbrainz.shared.util.AlbumsData

expect fun platform(): String

expect fun provideLogger(
    buildInfo: BuildInfo
): Logger

expect fun provideLogSubmitter(
    buildInfo: BuildInfo
): LogSubmitter

expect fun provideRemotePlaybackHandler(
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler

expect fun getBrainzPlayerDatabase(): RoomDatabase.Builder<BrainzPlayerDatabase>

expect fun provideListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher
): ListensRepository


expect fun getListensSubmissionDatabase(): RoomDatabase.Builder<ListensSubmissionDatabase>

expect fun provideSongData(): SongsData

expect fun provideAlbumsData(): AlbumsData