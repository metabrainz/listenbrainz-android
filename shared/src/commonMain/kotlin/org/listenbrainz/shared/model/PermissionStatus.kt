package org.listenbrainz.shared.model

enum class PermissionStatus {
    GRANTED,
    /** A request will show a system prompt or the relevant settings page (never asked, or declined once). */
    NOT_GRANTED,
    PERMANENTLY_DENIED
}