package org.listenbrainz.android.util.migrations

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys
import org.listenbrainz.android.util.DataStoreSerializers.stringListSerializer

val blacklistMigration: DataMigration<Preferences> =
    object: DataMigration<Preferences> {
        override suspend fun cleanUp() = Unit
        
        override suspend fun shouldMigrate(currentData: Preferences): Boolean {
            // If blacklist is deleted, then we are sure that migration took place.
            return currentData.contains(AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_BLACKLIST)
        }
        
        override suspend fun migrate(currentData: Preferences): Preferences {
            val blacklist = stringListSerializer.from(currentData[PreferenceKeys.LISTENING_BLACKLIST] ?: "")
            val appList = stringListSerializer.from(currentData[PreferenceKeys.LISTENING_APPS] ?: "")
            
            val whitelist = stringListSerializer.from(currentData[PreferenceKeys.LISTENING_WHITELIST] ?: "").toMutableSet()
            appList.forEach { pkg ->
                if (!blacklist.contains(pkg)) {
                    whitelist.add(pkg)
                }
            }
            
            val mutablePreferences = currentData.toMutablePreferences()
            mutablePreferences[PreferenceKeys.LISTENING_WHITELIST] = Gson().toJson(whitelist.toList())
            mutablePreferences.remove(PreferenceKeys.LISTENING_BLACKLIST)  // Clear old stale data and key.
            
            return mutablePreferences.toPreferences()
        }
    }