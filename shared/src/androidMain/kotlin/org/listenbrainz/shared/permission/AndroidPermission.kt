package org.listenbrainz.shared.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_battery
import org.listenbrainz.shared.resources.ic_notification_read
import org.listenbrainz.shared.ui.screens.onboarding.permissions.PermissionInfo
import org.listenbrainz.shared.ui.screens.onboarding.permissions.SendNotificationsInfo

enum class AndroidPermission(
    override val id: String,
    override val info: PermissionInfo,
    internal val spec: AndroidPermissionSpec
) : AppPermission {
    SEND_NOTIFICATIONS(
        id = "send_notification",
        info = SendNotificationsInfo,
        spec = AndroidPermissionSpec.Runtime(
            manifestPermission = Manifest.permission.POST_NOTIFICATIONS,
            minSdk = 33
        )
    ),

    READ_NOTIFICATIONS(
        id = "read_notifications",
        info = PermissionInfo(
            title = "Read Notifications",
            rationaleText = "Allows ListenBrainz to detect songs from other apps and submit them automatically.",
            permanentlyDeclinedRationale = "Without it, automatic music tracking from other apps won’t work.",
            image = Res.drawable.ic_notification_read,
        ),
        spec = AndroidPermissionSpec.SpecialAccess(
            minSdk = 21,
            // If granted, the string contains the listener service, so we check for our package name
            isGranted = { context ->
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                    ?.contains(context.packageName) == true
            },
            settingsIntent = { Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        )
    ),

    BATTERY_OPTIMIZATION(
        id = "battery_optimization",
        info = PermissionInfo(
            title = "Battery Optimization",
            rationaleText = "Disabling optimization ensures listens are submitted in the background without interruptions.",
            permanentlyDeclinedRationale = "With optimization enabled, background listening submission may fail or get delayed.",
            image = Res.drawable.ic_battery,
        ),
        spec = AndroidPermissionSpec.SpecialAccess(
            minSdk = 23,
            isGranted = { context ->
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            },
            settingsIntent = { context ->
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }
        )
    )
}
