package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.AndroidFileLogWriter
import org.listenbrainz.shared.util.AndroidLogSubmitter
import org.listenbrainz.shared.util.BuildInfo
import org.listenbrainz.shared.util.LogSubmitter

actual fun platform() = "Android"


actual fun provideLogger(
    context: PlatformContext?,
    buildInfo: BuildInfo?
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    val logFileDirectory = context?.getExternalFilesDir(null)?.path

    if(logFileDirectory != null && buildInfo != null){
        writers.add(AndroidFileLogWriter(logFileDirectory, buildInfo))
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