package org.listenbrainz.android.ui.screens.onboarding.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import org.listenbrainz.android.R

/**
 * Enum class to manage permissions in the app.
 * It contains the permission name, title, rationale text, and image resource for each permission.
 * Simply add a new enum constant for each permission you want to manage.
 */
enum class PermissionEnum(
    val permission: String,
    val title: String,
    val permanentlyDeclinedRationale: String,
    val rationaleText: String,
    val image: Int,
    val minSdk: Int,
    val maxSdk: Int? = null
) {
    SEND_NOTIFICATIONS(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        title = "Send Notifications",
        permanentlyDeclinedRationale = "Without notifications, we can't alert you about new features, errors, or background activity.",
        rationaleText = "Needed to send updates on activity, recommendations, and system alerts for a better user experience.",
        image = R.drawable.ic_notification,
        minSdk = 33
    ),

    ACCESS_MUSIC_AUDIO(
        permission = Manifest.permission.READ_MEDIA_AUDIO,
        title = "Access Music & Audio Files",
        permanentlyDeclinedRationale = "Without access, BrainzPlayer can't play your local music stored on the device.",
        rationaleText = "Required to play, browse, and manage your local audio files in BrainzPlayer seamlessly.",
        image = R.drawable.ic_audio_file,
        minSdk = 33
    ),

    READ_EXTERNAL_STORAGE(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
        title = "Read External Storage",
        permanentlyDeclinedRationale = "This permission is needed to access and play your local music files.",
        rationaleText = "Lets BrainzPlayer read your stored music for browsing and playback within the app.",
        image = R.drawable.ic_storage,
        minSdk = 23,
        maxSdk = 32
    ),

    READ_NOTIFICATIONS(
        permission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
        title = "Read Notifications",
        permanentlyDeclinedRationale = "Without it, automatic music tracking from other apps won’t work.",
        rationaleText = "Allows ListenBrainz to detect songs from other apps and submit them automatically.",
        image = R.drawable.ic_notification_read,
        minSdk = 21
    ),

    BATTERY_OPTIMIZATION(
        permission = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
        title = "Battery Optimization",
        permanentlyDeclinedRationale = "With optimization enabled, background listening submission may fail or get delayed.",
        rationaleText = "Disabling optimization ensures listens are submitted in the background without interruptions.",
        image = R.drawable.ic_battery,
        minSdk = 23
    ),

    WRITE_EXTERNAL_STORAGE(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        title = "Write External Storage",
        permanentlyDeclinedRationale = "Required to manage and access your saved music files.",
        rationaleText = "Needed to store, organize, and play music files from your device.",
        image = R.drawable.ic_storage,
        minSdk = 23,
        //Maximum sdk update to 28, as scoped storage replaces it from API 29, even though not strictly in API 29
        maxSdk = 28
    );

    //This function checks if the permission is applicable for the current Android version
    fun isPermissionApplicable(): Boolean{
        return if(Build.VERSION.SDK_INT >= minSdk){
            maxSdk?.let {
                Build.VERSION.SDK_INT <= it
            }?: true
        }else{
            false
        }
    }

    //This function assumes that permission was requested atleast one time (according to working of shouldShowRequestPermissionRationale)
    fun isPermissionPermanentlyDeclined(activity: Activity, permissionsRequestedOnce: List<String>): Boolean{
        if(!isPermissionApplicable()) return false
        return when(this){
            SEND_NOTIFICATIONS, ACCESS_MUSIC_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE ->{
                if(permissionsRequestedOnce.contains(permission) && Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
                    //If the permission was requested once, then we can check if it is permanently declined
                  !activity.shouldShowRequestPermissionRationale(permission) && ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
                else false
            }
            READ_NOTIFICATIONS, BATTERY_OPTIMIZATION->false
        }
    }

    //This function checks if the permission is granted or not
    //If a permission is not applicable for the current Android version, it will return true
    fun isGranted(context: Context): Boolean {
        if(!isPermissionApplicable()) return true
        return when(this){
            SEND_NOTIFICATIONS, ACCESS_MUSIC_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE ->
                //Normal way of checking permissions
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            READ_NOTIFICATIONS->{
                //If permission is granted, the string will contain "org.listenbrainz.android.service.ListenSubmissionService", so we'll check if the package name is present in the string
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")?.contains(context.packageName) == true
            }
            BATTERY_OPTIMIZATION->{
                //This condition is already checked in the applicablePermission function, just adding here to remove lint warning
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
    }

    //Requests permission and also handles situation if the permission is permanently declined
    fun requestPermission(activity: Activity, permissionsRequestedOnce: List<String>, dangerousPermissionLauncher: (permission: String)->Unit) {
        if(!isPermissionApplicable()) return

        when(this){
            SEND_NOTIFICATIONS, ACCESS_MUSIC_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE ->{
                if(isPermissionPermanentlyDeclined(activity, permissionsRequestedOnce)){
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${activity.packageName}".toUri()
                    }
                    activity.startActivity(intent)
                }else {
                    dangerousPermissionLauncher(permission)
                }
            }
            READ_NOTIFICATIONS -> {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                activity.startActivity(intent)
            }
            BATTERY_OPTIMIZATION -> {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${activity.packageName}".toUri()
                }
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                }
            }
        }
    }

    companion object{
        /// Function to get the list of required permissions based on the current Android version
        fun getAllRelevantPermissions(): List<PermissionEnum> = PermissionEnum.entries.filter {
            it.isPermissionApplicable()
        }
        fun getPermissionsForPermissionScreen(): List<PermissionEnum> = PermissionEnum.entries.filter {
            it.isPermissionApplicable() &&
            it != READ_NOTIFICATIONS && // READ_NOTIFICATIONS is handled separately
            it != BATTERY_OPTIMIZATION // BATTERY_OPTIMIZATION is handled separately
        }

        fun getListOfPermissionsToBeLaunchedTogether(context: Activity, permissionsRequestedOnce: List<String>): Array<String>{
            return getPermissionsForPermissionScreen().filter { permission->
                !permission.isGranted(context) && !permission.isPermissionPermanentlyDeclined(context, permissionsRequestedOnce)
            }.map { it.permission }.toList().toTypedArray()
        }
    }
}