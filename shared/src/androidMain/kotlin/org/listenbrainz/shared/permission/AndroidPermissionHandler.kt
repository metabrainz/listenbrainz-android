package org.listenbrainz.shared.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import org.listenbrainz.shared.applicationContext
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.ui.screens.onboarding.permissions.AppPermission
import org.listenbrainz.shared.ui.screens.onboarding.permissions.SharedPermissionEnum

class AndroidPermissionEnumHandler(
    private val context: PlatformContext = applicationContext
) : PermissionHandler {

    /// Function to get the list of required permissions based on the current Android version
    override fun getAllRelevantPermissions(): List<AppPermission> =
        allPermissions.filter { it.isPermissionApplicable() }

    override fun getPermissionsForPermissionScreen(): List<AppPermission> =
        getAllRelevantPermissions().filter {
            it != AndroidPermissionEnum.READ_NOTIFICATIONS && // READ_NOTIFICATIONS is handled separately
            it != AndroidPermissionEnum.BATTERY_OPTIMIZATION // BATTERY_OPTIMIZATION is handled separately
        }

    override fun isPermissionApplicable(permission: AppPermission): Boolean =
        permission.isPermissionApplicable()

    //This function checks if the permission is granted or not
    //If a permission is not applicable for the current Android version, it will return true
    override suspend fun isGranted(permission: AppPermission): Boolean {
        if (!permission.isPermissionApplicable()) return true
        return when (permission) {
            AndroidPermissionEnum.READ_NOTIFICATIONS -> {
                //If permission is granted, the string will contain "org.listenbrainz.android.service.ListenSubmissionService", so we'll check if the package name is present in the string
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.contains(context.packageName) == true
            }

            AndroidPermissionEnum.BATTERY_OPTIMIZATION -> {
                //This condition is already checked in applicablePermission function, just adding here to remove lint warning
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }

            //Normal way of checking permissions
            else -> ContextCompat.checkSelfPermission(
                context,
                permission.systemPermission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    //The system permission string is persisted, so the flag survives renaming a permission constant
    override fun storageKey(permission: AppPermission): String = permission.systemPermission

    //Requests permission and also handles situation if the permission is permanently declined
    override suspend fun requestPermission(
        permission: AppPermission,
        activity: Any?,
        permissionRequestedOnce: List<String>,
        dangerousPermissionLauncher: (permission: String) -> Unit
    ): Boolean? {
        val activity = activity as? Activity
        if(!permission.isPermissionApplicable()) return true

        when(permission){
            SharedPermissionEnum.SEND_NOTIFICATIONS ->{
                if(activity != null && permission.isPermissionPermanentlyDeclined(activity,permissionRequestedOnce)){
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${activity.packageName}".toUri()
                    }
                    startActivitySafely(activity,intent)
                }else {
                    dangerousPermissionLauncher(permission.systemPermission)
                }
            }
            AndroidPermissionEnum.READ_NOTIFICATIONS -> {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                startActivitySafely(activity,intent)
            }

            AndroidPermissionEnum.BATTERY_OPTIMIZATION -> {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    startActivitySafely(activity,intent)
                }
            }
        }
        return null
    }

    private fun startActivitySafely(activity: Activity?, intent: Intent){
        if(activity!=null){
            activity.startActivity(intent)
        }
        else{
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    companion object {
        private val allPermissions: List<AppPermission> =
            SharedPermissionEnum.entries + AndroidPermissionEnum.entries
    }
}
