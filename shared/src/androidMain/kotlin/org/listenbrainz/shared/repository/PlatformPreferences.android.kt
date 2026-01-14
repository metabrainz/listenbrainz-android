package org.listenbrainz.shared.repository

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import org.listenbrainz.shared.preferences.AndroidDataStoreContext
import org.listenbrainz.shared.preferences.PreferenceKeys
actual typealias PlatformContext = Context

actual fun platformDataMigrations(
    context: PlatformContext,
    baseMigrations: List<DataMigration<Preferences>>
): List<DataMigration<Preferences>> =
    listOf(
        SharedPreferencesMigration(
            context,
            context.packageName + "_preferences",
            setOf(
                PreferenceKeys.LB_ACCESS_TOKEN.name,
                PreferenceKeys.USERNAME.name,
                PreferenceKeys.REFESH_TOKEN.name,
                PreferenceKeys.LINKED_SERVICES.name,
                PreferenceKeys.THEME.name,
                PreferenceKeys.LISTENING_APPS.name,
                PreferenceKeys.LISTENING_BLACKLIST.name,
                PreferenceKeys.LISTENING_WHITELIST.name,
                PreferenceKeys.IS_LISTENING_ALLOWED.name,
                PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS.name,
                PreferenceKeys.PERMISSIONS_REQUESTED.name,
                PreferenceKeys.CONSENT_SCREEN_CACHE.name,
                PreferenceKeys.INSTALL_SOURCE.name,
                PreferenceKeys.APP_LAUNCH_COUNT.name,
                PreferenceKeys.LAST_VERSION_CHECK_LAUNCH_COUNT.name,
                PreferenceKeys.LAST_UPDATE_PROMPT_LAUNCH_COUNT.name,
                PreferenceKeys.GITHUB_DOWNLOAD_ID.name,
                PreferenceKeys.ALBUMS_ON_DEVICE.name,
                PreferenceKeys.SONGS_ON_DEVICE.name,
                PreferenceKeys.CURRENT_PLAYABLE.name,
                PreferenceKeys.ONBOARDING.name
            )
        )
    ) + baseMigrations

actual fun platformInitDataStoreContext(context: PlatformContext) {
    AndroidDataStoreContext.set(context)
}

actual fun platformPackageVersion(context: PlatformContext): String =
    try {
        context.packageManager?.getPackageInfo(context.packageName, 0)?.versionName ?: "N/A"
    } catch (e: PackageManager.NameNotFoundException) {
        "unknown"
    }

actual fun platformIsNotificationServiceAllowed(context: PlatformContext): Boolean {
    val listeners =
        Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return listeners != null && listeners.contains(context.packageName)
}
