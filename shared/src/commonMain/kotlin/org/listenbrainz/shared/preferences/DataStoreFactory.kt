package org.listenbrainz.shared.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Creates a DataStore instance.
 * 
 * @param producePath A function that returns the file path where the DataStore should be stored.
 *                    On Android this would be context.filesDir.resolve("datastore/$fileName").absolutePath
 *                    On iOS this would be NSHomeDirectory() + "/Documents/$fileName"
 */
expect fun createDataStore(
    producePath: () -> String,
    migrations: List<DataMigration<Preferences>> = emptyList()
): DataStore<Preferences>

/**
 * Default filename for preferences DataStore.
 */
const val DATA_STORE_FILE_NAME = "settings.preferences_pb"
