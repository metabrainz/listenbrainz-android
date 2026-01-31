package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
actual class PlatformContext

actual fun settingsPlatformDataMigrations(context: PlatformContext): List<DataMigration<Preferences>> = emptyList()

actual fun platformInitDataStoreContext(context: PlatformContext) = Unit

actual fun platformPackageVersion(context: PlatformContext): String = "unknown"

actual fun platformIsNotificationServiceAllowed(context: PlatformContext): Boolean = false
