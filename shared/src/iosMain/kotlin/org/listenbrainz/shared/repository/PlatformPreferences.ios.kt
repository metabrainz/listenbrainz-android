package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import platform.Foundation.NSBundle

actual class PlatformContext

actual fun getAppPackageName(context: PlatformContext): String {
    return NSBundle.mainBundle.bundleIdentifier ?: "org.listenbrainz.ios"
}

actual fun settingsPlatformDataMigrations(context: PlatformContext): List<DataMigration<Preferences>> = emptyList()

actual fun platformPackageVersion(context: PlatformContext): String = "unknown"

actual val isNotificationServiceAllowed: Boolean = false  // TODO
