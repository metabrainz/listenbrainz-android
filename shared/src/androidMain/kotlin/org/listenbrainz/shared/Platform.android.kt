package org.listenbrainz.shared

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter

actual fun platform() = "Android"


actual fun provideLogger(): Logger {

    val writers = mutableListOf(
        platformLogWriter()
    )

    return Logger(
        config = StaticConfig(
            minSeverity = Severity.Info,
            logWriterList = writers
        ),
        tag = "ListenBrainz"
    )
}