package org.listenbrainz.shared.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Creates a DataStore instance with a platform-resolved file path.
 *
 * @param name File name for the DataStore on disk.
 */
expect fun createDataStore(
    name: String,
    migrations: List<DataMigration<Preferences>> = emptyList()
): DataStore<Preferences>

/**
 * Default filename for preferences DataStore.
 */
const val DATA_STORE_FILE_NAME = "settings.preferences_pb"
