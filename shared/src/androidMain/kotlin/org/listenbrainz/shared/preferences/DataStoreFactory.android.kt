package org.listenbrainz.shared.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Android implementation of DataStore factory.
 */
actual fun createDataStore(
    producePath: () -> String,
    migrations: List<DataMigration<Preferences>>
): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
        migrations = migrations
    )
