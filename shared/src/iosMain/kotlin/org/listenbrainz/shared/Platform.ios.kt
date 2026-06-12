package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.IosFileLogWriter
import org.listenbrainz.shared.util.IosLogSubmitter
import org.listenbrainz.shared.util.LogSubmitter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.repository.remoteplayer.IosRemotePlaybackHandlerImpl
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.service.YouTubeApiService

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
    return IosRemotePlaybackHandlerImpl(appContext,youTubeApiService)
}