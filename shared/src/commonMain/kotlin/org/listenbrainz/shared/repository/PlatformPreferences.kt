package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

expect abstract class PlatformContext

expect fun platformDataMigrations(
    context: PlatformContext,
    baseMigrations: List<DataMigration<Preferences>>
): List<DataMigration<Preferences>>

expect fun platformInitDataStoreContext(context: PlatformContext)

expect fun platformPackageVersion(context: PlatformContext): String

expect fun platformIsNotificationServiceAllowed(context: PlatformContext): Boolean
