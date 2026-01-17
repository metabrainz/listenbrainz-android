package org.listenbrainz.shared.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of DataStore factory.
 */
actual fun createDataStore(
    name: String,
    migrations: List<DataMigration<Preferences>>
): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { iosDataStorePath(name).toPath() },
        migrations = migrations
    )

/**
 * Helper function to get the iOS documents directory path.
 */
@OptIn(ExperimentalForeignApi::class)
private fun iosDataStorePath(fileName: String): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return "${documentDirectory?.path}/$fileName.preferences_pb"
}
