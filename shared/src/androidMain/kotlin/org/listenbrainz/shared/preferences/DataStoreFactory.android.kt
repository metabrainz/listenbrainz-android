package org.listenbrainz.shared.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import okio.Path.Companion.toPath

/**
 * Android implementation of DataStore factory.
 */
actual fun createDataStore(
    name: String,
    migrations: List<DataMigration<Preferences>>
): DataStore<Preferences> {
    val context = AndroidDataStoreContext.require()
    val path = context.preferencesDataStoreFile(name).absolutePath
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { path.toPath() },
        migrations = migrations
    )
}
