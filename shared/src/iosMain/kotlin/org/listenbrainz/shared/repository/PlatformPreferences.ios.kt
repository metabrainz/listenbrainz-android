package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
actual class PlatformContext

actual fun platformDataMigrations(
    context: PlatformContext,
    baseMigrations: List<DataMigration<Preferences>>
): List<DataMigration<Preferences>> = baseMigrations

actual fun platformInitDataStoreContext(context: PlatformContext) = Unit

actual fun platformPackageVersion(context: PlatformContext): String = "unknown"

actual fun platformIsNotificationServiceAllowed(context: PlatformContext): Boolean = false
