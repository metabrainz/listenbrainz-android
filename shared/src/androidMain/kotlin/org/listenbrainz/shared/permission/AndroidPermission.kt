package org.listenbrainz.shared.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import org.listenbrainz.shared.ui.screens.onboarding.permissions.AppPermission
import org.listenbrainz.shared.ui.screens.onboarding.permissions.SharedPermissionEnum
import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_battery
import org.listenbrainz.shared.resources.ic_notification_read

enum class AndroidPermissionEnum(
    override val id: String,
    override val title: String,
    override val permanentlyDeclinedRationale: String,
    override val rationaleText: String,
    override val image: DrawableResource,
    val systemPermission: String,
    val minSdk: Int,
    val maxSdk: Int? = null
) : AppPermission {
    READ_NOTIFICATIONS(
        id = "read_notifications",
        title = "Read Notifications",
        permanentlyDeclinedRationale = "Without it, automatic music tracking from other apps won’t work.",
        rationaleText = "Allows ListenBrainz to detect songs from other apps and submit them automatically.",
        image = Res.drawable.ic_notification_read,
        systemPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
        minSdk = 21,
    ),

    BATTERY_OPTIMIZATION(
        id = "battery_optimization",
        title = "Battery Optimization",
        permanentlyDeclinedRationale = "With optimization enabled, background listening submission may fail or get delayed.",
        rationaleText = "Disabling optimization ensures listens are submitted in the background without interruptions.",
        image = Res.drawable.ic_battery,
        systemPermission = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
        minSdk = 23
    );
}

data class AndroidPermissionEnumSpec(
    val systemPermission: String,
    val minSdkInclusive: Int,
    val maxSdkInclusive: Int? = null
)

val AppPermission.androidSpec: AndroidPermissionEnumSpec
    get() = when (this) {
        is AndroidPermissionEnum -> AndroidPermissionEnumSpec(systemPermission, minSdk, maxSdk)
        SharedPermissionEnum.SEND_NOTIFICATIONS -> AndroidPermissionEnumSpec(
            systemPermission = Manifest.permission.POST_NOTIFICATIONS,
            minSdkInclusive = 33
        )
        else -> error("Unknown permission: $id")
    }

val AppPermission.systemPermission: String
    get() = androidSpec.systemPermission

//This function checks if the permission is applicable for the current Android version
fun AppPermission.isPermissionApplicable(): Boolean {
    val spec = androidSpec
    return Build.VERSION.SDK_INT >= spec.minSdkInclusive
            && (spec.maxSdkInclusive == null || spec.maxSdkInclusive >= Build.VERSION.SDK_INT)
}

//This function assumes that permission was requested atleast one time (according to working of shouldShowRequestPermissionRationale)
fun AppPermission.isPermissionPermanentlyDeclined(
    activity: Activity,
    permissionsRequestedOnce: List<String>
): Boolean {
    return isPermissionApplicable() && when (this) {
        AndroidPermissionEnum.READ_NOTIFICATIONS, AndroidPermissionEnum.BATTERY_OPTIMIZATION -> false
        else -> {
            //If the permission was requested once, then we can check if it is permanently declined
            permissionsRequestedOnce.contains(systemPermission)
                    && !activity.shouldShowRequestPermissionRationale(systemPermission)
                    && ContextCompat.checkSelfPermission(
                    activity,
                    systemPermission
                ) != PackageManager.PERMISSION_GRANTED
        }
    }
}