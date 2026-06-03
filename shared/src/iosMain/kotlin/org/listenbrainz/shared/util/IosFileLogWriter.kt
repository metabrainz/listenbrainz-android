package org.listenbrainz.shared.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

class IosFileLogWriter(
    logDirectory: String,
    private val buildConfig : BuildInfo
) : LogWriter(), SharedFileLogWriter {

    // ios implementation here

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        TODO("Not yet implemented")
    }

}