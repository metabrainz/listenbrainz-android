package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.IosFileLogWriter
import org.listenbrainz.shared.util.IosLogSubmitter
import org.listenbrainz.shared.util.LogSubmitter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import org.listenbrainz.shared.repository.listens.IosListensRepositoryImpl
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.IosRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService
import platform.posix.err
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase
import org.listenbrainz.shared.util.IosSongsData
import org.listenbrainz.shared.util.SongsData
import org.listenbrainz.shared.util.AlbumsData
import org.listenbrainz.shared.util.IosAlbumsData
import platform.Foundation.NSFileManager

actual fun platform() = "iOS"

actual fun provideLogger(
    context: PlatformContext,
    buildInfo: BuildInfo
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    val logFileDirectory = NSSearchPathForDirectoriesInDomains(
        directory = NSDocumentDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).firstOrNull() as? String

    if(logFileDirectory != null){
        writers.add(IosFileLogWriter(logFileDirectory, buildInfo))
    }

    return Logger(
        config = StaticConfig(
            minSeverity = Severity.Debug,
            logWriterList = writers
        ),
        tag = "ListenBrainz"
    )
}

actual fun provideLogSubmitter(context: PlatformContext, buildInfo: BuildInfo): LogSubmitter {
    return IosLogSubmitter(buildInfo)
}

actual fun provideRemotePlaybackHandler(
    appContext: PlatformContext,
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler {
    return IosRemotePlaybackHandlerImpl(youTubeApiService)
}

actual fun getBrainzPlayerDatabase(context: PlatformContext): RoomDatabase.Builder<BrainzPlayerDatabase> {
    val brainzPlayerDB = documentDirectory() + "/brainzplayer_database.db"
    return Room.databaseBuilder<BrainzPlayerDatabase>(
        name = brainzPlayerDB
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

actual fun provideListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher,
    appContext: PlatformContext
): ListensRepository {
    return IosListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher,appContext)
}

actual fun getListensSubmissionDatabase(appContext: PlatformContext): RoomDatabase.Builder<ListensSubmissionDatabase> {
    val listensDB = documentDirectory() + "/listens_scrobble_database.db"
    return Room.databaseBuilder<ListensSubmissionDatabase>(
        name = listensDB
    )
}

actual fun provideSongData(context: PlatformContext): SongsData {
    return IosSongsData()
}

actual fun provideAlbumsData(context: PlatformContext): AlbumsData {
    return IosAlbumsData()
}