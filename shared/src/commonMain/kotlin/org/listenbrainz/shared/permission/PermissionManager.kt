package org.listenbrainz.shared.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet
import org.listenbrainz.shared.model.PermissionStatus

abstract class PermissionManager {
    /**
     * Live status of every permission that exists on this platform and OS version. Never persisted:
     *  derived from the OS, refreshed whenever the app comes to the foreground and after every request.
     *  Empty until the first refresh completes.
     */
    val statuses: StateFlow<Map<AppPermission, PermissionStatus>>
        field = MutableStateFlow(emptyMap())

    /** The permissions this platform needs for [scope]. Empty when the platform has nothing to ask for.
     * Define your own scope in [PermissionScope] for your use-case to keep behaviour deterministic. */
    abstract fun permissionsFor(scope: PermissionScope): List<AppPermission>

    abstract suspend fun refresh(): Map<AppPermission, PermissionStatus>

    /** Shows the system prompt, or opens the relevant settings page when a prompt isn't possible.
     *  Returns the status afterwards, for settings flows the real result arrives on the next foreground refresh. */
    abstract suspend fun request(permission: AppPermission): PermissionStatus

    protected fun publish(
        updated: Map<out AppPermission, PermissionStatus>
    ): Map<AppPermission, PermissionStatus> = statuses.updateAndGet { it + updated }
}
