package org.listenbrainz.shared.util

import co.touchlab.kermit.Severity

class IosFileLogWriter(
    logDirectory: String,
     buildConfig : BuildInfo
) : SharedFileLogWriter(buildConfig) {

    // ios implementation here

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun writeLineToFile(entry: String) {
        TODO("Not yet implemented")
    }

}