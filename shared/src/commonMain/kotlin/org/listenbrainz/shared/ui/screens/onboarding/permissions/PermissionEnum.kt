package org.listenbrainz.shared.ui.screens.onboarding.permissions

import org.listenbrainz.shared.util.DrawableResource


interface AppPermission {
    val id: String
    val title: String
    val permanentlyDeclinedRationale: String
    val rationaleText: String
    val image: DrawableResource
}

/**
 * Enum class to manage permissions in the app.
 * It contains the unique id, title, rationale text, and image resource for each permission.
 * Simply add a new enum constant for each permission you want to manage.
 */
enum class SharedPermissionEnum(
    override val title: String,
    override val id: String,
    override val permanentlyDeclinedRationale: String,
    override val rationaleText: String,
    override val image: DrawableResource,
) : AppPermission {
    SEND_NOTIFICATIONS(
        id = "send_notification",
        title = "Send Notifications",
        permanentlyDeclinedRationale = "Without notifications, we can't alert you about new features, errors, or background activity.",
        rationaleText = "Needed to send updates on activity, recommendations, and system alerts for a better user experience.",
        image = DrawableResource.IC_NOTIFICATION,
    )
}