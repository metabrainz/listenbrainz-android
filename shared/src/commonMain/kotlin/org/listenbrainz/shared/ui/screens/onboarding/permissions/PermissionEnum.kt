package org.listenbrainz.shared.ui.screens.onboarding.permissions

import org.jetbrains.compose.resources.DrawableResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_notification

data class PermissionInfo(
    val title: String,
    val rationaleText: String,
    val permanentlyDeclinedRationale: String,
    val image: DrawableResource,
)

internal val SendNotificationsInfo = PermissionInfo(
    title = "Send Notifications",
    rationaleText = "Needed to send updates on activity, recommendations, and system alerts for a better user experience.",
    permanentlyDeclinedRationale = "Without notifications, we can't alert you about new features, errors, or background activity.",
    image = Res.drawable.ic_notification,
)
