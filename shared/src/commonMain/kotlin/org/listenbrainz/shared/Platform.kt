package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.util.SongsData

expect fun platform(): String

expect fun provideLogger(
    context: PlatformContext,
    buildInfo: BuildInfo
): Logger

expect fun provideLogSubmitter(
    context: PlatformContext,
    buildInfo: BuildInfo
): LogSubmitter

expect fun provideRemotePlaybackHandler(
    appContext: PlatformContext,
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler

expect fun getBrainzPlayerDatabase(context: PlatformContext): RoomDatabase.Builder<BrainzPlayerDatabase>

expect fun provideListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher,
    appContext: PlatformContext
): ListensRepository


expect fun getListensSubmissionDatabase(
    appContext: PlatformContext
): RoomDatabase.Builder<ListensSubmissionDatabase>

expect fun provideSongData(
    context: PlatformContext
): SongsData