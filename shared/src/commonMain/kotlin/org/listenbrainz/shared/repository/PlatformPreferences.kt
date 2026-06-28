package org.listenbrainz.shared.repository

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

expect abstract class PlatformContext

expect fun getAppPackageName(context: PlatformContext): String
expect fun settingsPlatformDataMigrations(
    context: PlatformContext,
): List<DataMigration<Preferences>>

expect fun platformPackageVersion(context: PlatformContext): String

expect val isNotificationServiceAllowed: Boolean
