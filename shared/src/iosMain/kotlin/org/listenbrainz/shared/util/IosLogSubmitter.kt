package org.listenbrainz.shared.util


class IosLogSubmitter(
    private val buildConfig: BuildInfo
): LogSubmitter{
    override suspend fun submitLogs() {
        TODO("Not yet implemented")
        // ios implementation here
    }
}