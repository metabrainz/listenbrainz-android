package org.listenbrainz.shared.util

import kotlinx.coroutines.suspendCancellableCoroutine
import org.listenbrainz.shared.repository.PlatformContext
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

actual object PlatformUtils {
    actual fun getSHA1(context: PlatformContext, packageName: String): String? {
        return null
    }
    actual suspend fun canShowNotifications(): Boolean {
        return suspendCancellableCoroutine{
            val center = UNUserNotificationCenter.currentNotificationCenter()
            center.getNotificationSettingsWithCompletionHandler { settings->
                val status = settings?.authorizationStatus
                it.resume(
                    status == UNAuthorizationStatusAuthorized ||
                            status == UNAuthorizationStatusProvisional ||
                            status == UNAuthorizationStatusEphemeral
                )
            }
        }
    }
}