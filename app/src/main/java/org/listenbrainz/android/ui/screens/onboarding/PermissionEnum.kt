package org.listenbrainz.android.ui.screens.onboarding

import android.annotation.SuppressLint
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
@SuppressLint("NewApi")
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
        permission = android.Manifest.permission.POST_NOTIFICATIONS,
        title = "Send Notifications",
        permanentlyDeclinedRationale = "Without notifications, we can't alert you about new features, errors, or background activity.",
        rationaleText = "Needed to send updates on activity, recommendations, and system alerts for a better user experience.",
        image = R.drawable.ic_notification,
        minSdk = 33
    ),

    ACCESS_MUSIC_AUDIO(
        permission = android.Manifest.permission.READ_MEDIA_AUDIO,
        title = "Access Music & Audio Files",
        permanentlyDeclinedRationale = "Without access, BrainzPlayer can't play your local music stored on the device.",
        rationaleText = "Required to play, browse, and manage your local audio files in BrainzPlayer seamlessly.",
        image = R.drawable.ic_audio_file,
        minSdk = 33
    ),

    READ_EXTERNAL_STORAGE(
        permission = android.Manifest.permission.READ_EXTERNAL_STORAGE,
        title = "Read External Storage",
        permanentlyDeclinedRationale = "This permission is needed to access and play your local music files.",
        rationaleText = "Lets BrainzPlayer read your stored music for browsing and playback within the app.",
        image = R.drawable.ic_storage,
        minSdk = 19,
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
        permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        title = "Write External Storage",
        permanentlyDeclinedRationale = "Required to manage and access your saved music files.",
        rationaleText = "Needed to store, organize, and play music files from your device.",
        image = R.drawable.ic_storage,
        minSdk = 19,
        maxSdk = 32
    );

    //This function checks if the permission is applicable for the current Android version
    private fun isPermissionApplicable(): Boolean{
        return if(Build.VERSION.SDK_INT >= minSdk){
            maxSdk?.let {
                Build.VERSION.SDK_INT <= it
            }?: true
        }else{
            false
        }
    }

    //This function assumes that permission was requested atleast one time (according to working of shouldShowRequestPermissionRationale)
    fun isPermissionPermanentlyDeclined(activity: Activity): Boolean{
        if(!isPermissionApplicable()) return false
        return when(this){
            SEND_NOTIFICATIONS, ACCESS_MUSIC_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE ->{
              !activity.shouldShowRequestPermissionRationale(permission) && ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
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
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
    }

    //Requests permission and also handles situation if the permission is permanently declined
    fun requestPermission(activity: Activity, dangerousPermissionLauncher: (permission: String)->Unit) {
        if(!isPermissionApplicable()) return

        when(this){
            SEND_NOTIFICATIONS, ACCESS_MUSIC_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE ->{
                if(isPermissionPermanentlyDeclined(activity)){
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
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                activity.startActivity(intent)
            }
        }
    }

    companion object{
        /// Function to get the list of required permissions based on the current Android version
        fun getRequiredPermissionsList(): List<PermissionEnum> = PermissionEnum.entries.filter {
            it.isPermissionApplicable()
        }
    }
}