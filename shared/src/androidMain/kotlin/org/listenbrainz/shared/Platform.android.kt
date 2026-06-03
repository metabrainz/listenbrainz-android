package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import org.listenbrainz.shared.util.AndroidFileLogWriter
import org.listenbrainz.shared.util.BuildInfo

actual fun platform() = "Android"


actual fun provideLogger(
    logFileDirectory:String?,
    buildInfo: BuildInfo?
): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

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