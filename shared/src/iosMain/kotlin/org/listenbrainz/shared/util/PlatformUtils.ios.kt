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
                if(!it.isActive){
                    return@getNotificationSettingsWithCompletionHandler
                }
                if(settings!=null){
                    val status = settings.authorizationStatus
                    val isGranted = status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional || status == UNAuthorizationStatusEphemeral
                    it.resume(isGranted)
                } else{
                    it.resume(false)
                }
            }
        }
    }
}