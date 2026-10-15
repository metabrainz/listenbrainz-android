package org.listenbrainz.shared.permission

import org.listenbrainz.shared.ui.screens.onboarding.permissions.AppPermission

interface PermissionHandler {
    fun getAllRelevantPermissions(): List<AppPermission>
    fun getPermissionsForPermissionScreen(): List<AppPermission>
    fun isPermissionApplicable(permission: AppPermission): Boolean
    suspend fun isGranted(permission: AppPermission): Boolean
    fun storageKey(permission: AppPermission): String
    suspend fun requestPermission(
        permission: AppPermission,
        activity: Any? = null,
        permissionRequestedOnce: List<String> = emptyList(),
        dangerousPermissionLauncher: (permission: String) -> Unit = {}
    ): Boolean?
}
