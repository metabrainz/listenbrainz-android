package org.listenbrainz.shared.permission

import org.listenbrainz.shared.model.PermissionStatus
import org.listenbrainz.shared.ui.screens.onboarding.permissions.PermissionInfo

/**
 * A permission the app can ask for. Opaque to common code: each platform defines its own
 * permissions and only reports the ones it actually has.
 */
interface AppPermission {
    val id: String
    val info: PermissionInfo
}

/** The features that need permissions. Which permissions that means is up to the platform. */
enum class PermissionScope {
    /** Asked on the onboarding permission screen. */
    ONBOARDING,

    /** Asked in context on the listening apps screen and in Settings. */
    LISTEN_SUBMISSION
}

/**
 * The [permissions] that are not [PermissionStatus.GRANTED]; a permission missing from the map
 * (e.g. not loaded yet) counts as missing.
 * An empty result means "nothing left to grant", NOT "this feature exists on this platform".
 */
fun Map<AppPermission, PermissionStatus>.missing(permissions: List<AppPermission>): List<AppPermission> =
    permissions.filter { this[it] != PermissionStatus.GRANTED }
