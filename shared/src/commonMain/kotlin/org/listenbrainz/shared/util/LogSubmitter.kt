package org.listenbrainz.shared.util

import org.listenbrainz.shared.repository.PlatformContext

expect interface LogSubmitter {
    suspend fun submitLogs(context: PlatformContext)
}