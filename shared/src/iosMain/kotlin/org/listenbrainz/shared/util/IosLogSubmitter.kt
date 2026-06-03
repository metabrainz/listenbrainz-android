package org.listenbrainz.shared.util

import org.listenbrainz.shared.repository.PlatformContext

actual interface LogSubmitter{
    actual suspend fun submitLogs(context: PlatformContext)
}

class IosLogSubmitter(
    private val buildConfig: BuildInfo
): LogSubmitter{
    override suspend fun submitLogs(context: PlatformContext) {
        TODO("Not yet implemented")
        // ios implementation here
    }
}