package org.listenbrainz.shared.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import org.listenbrainz.shared.ui.screens.onboarding.permissions.AppPermission
import org.listenbrainz.shared.ui.screens.onboarding.permissions.SharedPermissionEnum
import org.listenbrainz.shared.util.PlatformUtils
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class IosPermissionHandler : PermissionHandler {

    override fun getAllRelevantPermissions(): List<AppPermission> = SharedPermissionEnum.entries

    override fun getPermissionsForPermissionScreen(): List<AppPermission> =
        getAllRelevantPermissions()

    override fun isPermissionApplicable(permission: AppPermission): Boolean =
        permission in SharedPermissionEnum.entries

    override suspend fun isGranted(permission: AppPermission): Boolean {
        if (!isPermissionApplicable(permission)) return true
        return when (permission) {
            SharedPermissionEnum.SEND_NOTIFICATIONS -> PlatformUtils.canShowNotifications()
            else -> true
        }
    }

    override fun storageKey(permission: AppPermission): String = permission.id

    override suspend fun requestPermission(
        permission: AppPermission,
        activity: Any?,
        permissionRequestedOnce: List<String>,
        dangerousPermissionLauncher: (permission: String) -> Unit
    ): Boolean? =
        when (permission) {
            SharedPermissionEnum.SEND_NOTIFICATIONS -> requestNotificationAuthorization()
            else -> true
        }
}

private suspend fun requestNotificationAuthorization(): Boolean =
    suspendCancellableCoroutine {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            completionHandler = { isGranted, _ ->
                if (it.isActive) {
                    it.resume(isGranted)
                }
            }
        )
    }
