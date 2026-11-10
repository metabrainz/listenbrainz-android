package org.listenbrainz.shared.permission

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.listenbrainz.shared.model.PermissionStatus
import org.listenbrainz.shared.ui.screens.onboarding.permissions.PermissionInfo
import org.listenbrainz.shared.ui.screens.onboarding.permissions.SendNotificationsInfo
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

enum class IosPermission(
    override val id: String,
    override val info: PermissionInfo
) : AppPermission {
    SEND_NOTIFICATIONS(id = "send_notification", info = SendNotificationsInfo)
}

class IosPermissionManager : PermissionManager() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Serialises status updates so an older, slower check can never overwrite a newer result
    private val mutex = Mutex()

    private val refreshTriggers = Channel<Unit>(Channel.CONFLATED)

    init {
        refreshTriggers.trySend(Unit)
        scope.launch {
            for (trigger in refreshTriggers) refresh()
        }
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null,
            queue = null
        ) { _ -> refreshTriggers.trySend(Unit) }
    }

    override fun permissionsFor(scope: PermissionScope): List<AppPermission> = when (scope) {
        PermissionScope.ONBOARDING -> listOf(IosPermission.SEND_NOTIFICATIONS)
        PermissionScope.LISTEN_SUBMISSION -> emptyList()
    }

    override suspend fun refresh(): Map<AppPermission, PermissionStatus> =
        updateStatuses(IosPermission.entries)

    private suspend fun updateStatuses(
        permissions: List<IosPermission>
    ): Map<AppPermission, PermissionStatus> = mutex.withLock {
        val statuses = permissions.associateWith { statusOf(it) }
        publish(statuses)
    }

    private suspend fun statusOf(permission: IosPermission): PermissionStatus = when (permission) {
        IosPermission.SEND_NOTIFICATIONS -> notificationStatus()
    }

    override suspend fun request(permission: AppPermission): PermissionStatus {
        require(permission is IosPermission) { "Not an iOS permission: ${permission.id}" }
        when (statusOf(permission)) {
            PermissionStatus.NOT_GRANTED -> when (permission) {
                IosPermission.SEND_NOTIFICATIONS -> requestNotificationAuthorization()
            }

            PermissionStatus.PERMANENTLY_DENIED -> openAppSettings()
            PermissionStatus.GRANTED -> Unit
        }
        // Only the requested permission can have changed; others are picked up on the next activation
        return updateStatuses(listOf(permission)).getValue(permission)
    }

    private suspend fun notificationStatus(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    val status = when (settings?.authorizationStatus) {
                        UNAuthorizationStatusAuthorized,
                        UNAuthorizationStatusProvisional,
                        UNAuthorizationStatusEphemeral -> PermissionStatus.GRANTED

                        UNAuthorizationStatusDenied -> PermissionStatus.PERMANENTLY_DENIED
                        else -> PermissionStatus.NOT_GRANTED
                    }
                    if (continuation.isActive) continuation.resume(status)
                }
        }

    private suspend fun requestNotificationAuthorization() {
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                completionHandler = { _, _ ->
                    if (continuation.isActive) continuation.resume(Unit)
                }
            )
        }
    }

    private suspend fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        suspendCancellableCoroutine { continuation ->
            UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }
}
