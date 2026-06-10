package org.listenbrainz.shared.util

interface LogSubmitter {
    suspend fun submitLogs()
}