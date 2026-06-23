package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import org.listenbrainz.shared.di.database.ListensSubmissionDatabase
import org.listenbrainz.shared.model.dao.PendingListensDao
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.ANDROID_LOG_DIR_NAME
import org.listenbrainz.shared.util.AndroidFileLogWriter
import org.listenbrainz.shared.util.AndroidLogSubmitter
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter
import java.io.File
import org.listenbrainz.shared.repository.listens.AndroidListensRepositoryImpl
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.AndroidRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.ListensService
import org.listenbrainz.shared.service.UserService
import org.listenbrainz.shared.service.YouTubeApiService
import org.listenbrainz.shared.di.database.BrainzPlayerDatabase

actual fun platform() = "Android"


actual fun provideLogger(
    context: PlatformContext,
    buildInfo: BuildInfo
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    val externalFilesDir = context.getExternalFilesDir(null)

    if(externalFilesDir != null){
        val logDir = File(externalFilesDir, ANDROID_LOG_DIR_NAME).apply { mkdirs() }
        writers.add(AndroidFileLogWriter(logDir.path, buildInfo))
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
    return AndroidLogSubmitter(context,buildInfo)
}


actual fun provideRemotePlaybackHandler(
    appContext: PlatformContext,
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler {
    return AndroidRemotePlaybackHandlerImpl(appContext,youTubeApiService)
}


actual fun getBrainzPlayerDatabase(context: PlatformContext): RoomDatabase.Builder<BrainzPlayerDatabase> {
    val listensDB = context.getDatabasePath("brainzplayer_database")
    return Room.databaseBuilder<BrainzPlayerDatabase>(
        context = context,
        name = listensDB.absolutePath
    )
}

actual fun provideListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher,
    appContext: PlatformContext
): ListensRepository {
    return AndroidListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher,appContext)
}

actual fun getListensSubmissionDatabase(appContext: PlatformContext): RoomDatabase.Builder<ListensSubmissionDatabase> {
    val listensDb = appContext.getDatabasePath("listens_scrobble_database")
    return Room.databaseBuilder<ListensSubmissionDatabase>(
        context = appContext,
        name = listensDb.absolutePath
    )
}