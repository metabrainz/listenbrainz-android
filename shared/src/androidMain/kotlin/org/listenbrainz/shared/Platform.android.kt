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
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.AndroidSongsData
import org.listenbrainz.shared.util.SongsData
import org.listenbrainz.shared.util.AlbumsData
import org.listenbrainz.shared.util.AndroidAlbumsData

actual fun platform() = "Android"

actual fun provideLogger(
    buildInfo: BuildInfo
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    val externalFilesDir = applicationContext.getExternalFilesDir(null)

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

actual fun provideLogSubmitter(buildInfo: BuildInfo): LogSubmitter {
    return AndroidLogSubmitter(buildConfig = buildInfo)
}


actual fun provideRemotePlaybackHandler(
    youTubeApiService: YouTubeApiService
): RemotePlaybackHandler {
    return AndroidRemotePlaybackHandlerImpl(youTubeApiService)
}


actual fun getBrainzPlayerDatabase(): RoomDatabase.Builder<BrainzPlayerDatabase> {
    val brainzPlayerDb = applicationContext.getDatabasePath("brainzplayer_database")
    return Room.databaseBuilder<BrainzPlayerDatabase>(
        context = applicationContext,
        name = brainzPlayerDb.absolutePath
    )
}

actual fun provideListensRepositoryImpl(
    service: ListensService,
    appPreferences: AppPreferences,
    userService: UserService,
    pendingListensDao: PendingListensDao,
    ioDispatcher: CoroutineDispatcher
): ListensRepository {
    return AndroidListensRepositoryImpl(service,appPreferences,userService,pendingListensDao,ioDispatcher)
}

actual fun getListensSubmissionDatabase(): RoomDatabase.Builder<ListensSubmissionDatabase> {
    val listensDb = applicationContext.getDatabasePath("listens_scrobble_database")
    return Room.databaseBuilder<ListensSubmissionDatabase>(
        context = applicationContext,
        name = listensDb.absolutePath
    )
}

actual fun provideSongData(): SongsData {
    return AndroidSongsData()
}

actual fun provideAlbumsData(): AlbumsData {
    return AndroidAlbumsData()
}