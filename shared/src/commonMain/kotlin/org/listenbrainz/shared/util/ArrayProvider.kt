package org.listenbrainz.shared.util


enum class ArrayResource {
    NOTIFICATION_IDLE_MESSAGES
}

interface ArrayProvider {
    fun getArray(res: ArrayResource): Int
}