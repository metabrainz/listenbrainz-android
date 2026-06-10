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

actual fun platform() = "iOS"

actual fun provideLogger(
    logFileDirectory: String?,
    buildInfo: BuildInfo?
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    if(logFileDirectory != null && buildInfo != null){
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