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
import org.listenbrainz.shared.permission.IosPermissionHandler
import org.listenbrainz.shared.permission.PermissionHandler
import org.listenbrainz.shared.repository.AppPreferences
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
import org.listenbrainz.shared.util.ArrayProvider
import org.listenbrainz.shared.util.DrawableProvider
import org.listenbrainz.shared.util.IosNotificationManager
import org.listenbrainz.shared.util.ListenSubmissionNotification
import org.listenbrainz.shared.util.PlatformNotificationManager
import org.listenbrainz.shared.util.StringProvider
import platform.Foundation.NSFileManager

actual fun platform() = "iOS"

actual fun provideLogger(
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

actual fun provideLogSubmitter(buildInfo: BuildInfo): LogSubmitter {
    return IosLogSubmitter(buildInfo)
}

actual fun provideRemotePlaybackHandler(
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler {
    return IosRemotePlaybackHandlerImpl(youTubeApiService)
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
    ioDispatcher: CoroutineDispatcher
): ListensRepository {
    return IosListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher)
}

actual fun getListensSubmissionDatabase(): RoomDatabase.Builder<ListensSubmissionDatabase> {
    val listensDB = documentDirectory() + "/listens_scrobble_database.db"
    return Room.databaseBuilder<ListensSubmissionDatabase>(
        name = listensDB
    )
}

actual fun provideSharedNotificationManager(
    drawableProvider: DrawableProvider,
    stringProvider: StringProvider,
    arrayProvider: ArrayProvider
): PlatformNotificationManager {

    return IosNotificationManager(
        notificationId = ListenSubmissionNotification.NOTIFICATION_ID.toString(),
        listeningTitle = "♫ Listening now",
    )
}

actual fun providePermissionHandler(): PermissionHandler {
    return IosPermissionHandler()
}