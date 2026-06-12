package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.ANDROID_LOG_DIR_NAME
import org.listenbrainz.shared.util.AndroidFileLogWriter
import org.listenbrainz.shared.util.AndroidLogSubmitter
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter
import java.io.File

import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.repository.remoteplayer.AndroidRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.YouTubeApiService

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