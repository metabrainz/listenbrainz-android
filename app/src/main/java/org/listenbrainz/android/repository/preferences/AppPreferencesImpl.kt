package org.listenbrainz.android.repository.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.model.UiMode.Companion.asUiMode
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.IS_LISTENING_ALLOWED
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_APPS
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_BLACKLIST
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_WHITELIST
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.THEME
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Constants.ONBOARDING
import org.listenbrainz.android.util.Constants.Strings.CURRENT_PLAYABLE
import org.listenbrainz.android.util.Constants.Strings.LB_ACCESS_TOKEN
import org.listenbrainz.android.util.Constants.Strings.LINKED_SERVICES
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_ALBUMS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_APPS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_BLACKLIST
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_WHITELIST
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTEN_NEW_PLAYERS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_PERMS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SONGS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SUBMIT_LISTENS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_THEME
import org.listenbrainz.android.util.Constants.Strings.REFRESH_TOKEN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.Constants.Strings.USERNAME
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.TypeConverter

class AppPreferencesImpl(private val context: Context): AppPreferences {
    companion object {
        private val gson = Gson()
        private val blacklistMigration: DataMigration<Preferences> =
            object: DataMigration<Preferences> {
                override suspend fun cleanUp() = Unit
            
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    // If blacklist is deleted, then we are sure that migration took place.
                    return currentData.contains(LISTENING_BLACKLIST)
                }
            
                override suspend fun migrate(currentData: Preferences): Preferences {
                    val blacklist = currentData[LISTENING_BLACKLIST].asStringList()
                    val appList = currentData[LISTENING_APPS].asStringList()
                    
                    val whitelist = currentData[LISTENING_WHITELIST].asStringList().toMutableSet()
                    appList.forEach { pkg ->
                        if (!blacklist.contains(pkg)) {
                            whitelist.add(pkg)
                        }
                    }
                    
                    val mutablePreferences = currentData.toMutablePreferences()
                    mutablePreferences[LISTENING_WHITELIST] = Gson().toJson(whitelist.toList())
                    mutablePreferences.remove(LISTENING_BLACKLIST)  // Clear old stale data and key.
                    
                    return mutablePreferences.toPreferences()
                }
            }
        
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "settings",
            produceMigrations = { context ->
                // Since we're migrating from SharedPreferences, add a migration based on the
                // SharedPreferences name
                listOf(SharedPreferencesMigration(
                    context,
                    context.packageName + "_preferences",
                    setOf(
                        LB_ACCESS_TOKEN,
                        USERNAME,
                        PREFERENCE_SYSTEM_THEME,
                        PREFERENCE_LISTENING_APPS
                    )
                ), blacklistMigration)
            }
        )
    
        private object PreferenceKeys {
            val LB_ACCESS_TOKEN = stringPreferencesKey(Constants.Strings.LB_ACCESS_TOKEN)
            val USERNAME = stringPreferencesKey(Constants.Strings.USERNAME)
            val LISTENING_BLACKLIST = stringPreferencesKey(PREFERENCE_LISTENING_BLACKLIST)
            val LISTENING_WHITELIST = stringPreferencesKey(PREFERENCE_LISTENING_WHITELIST)
            val THEME = stringPreferencesKey(PREFERENCE_SYSTEM_THEME)
            val LISTENING_APPS = stringPreferencesKey(PREFERENCE_LISTENING_APPS)
            val IS_LISTENING_ALLOWED = booleanPreferencesKey(PREFERENCE_SUBMIT_LISTENS)
            val SHOULD_LISTEN_NEW_PLAYERS = booleanPreferencesKey(PREFERENCE_LISTEN_NEW_PLAYERS)
        }
        
        fun String?.asStringList(): List<String> {
            return gson.fromJson(
                this,
                object: TypeToken<List<String>>() {}.type
            ) ?: emptyList()
        }
    }
    
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    // Helper Functions
    
    private fun setString(key: String?, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }
    private fun setInteger(key: String?, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }
    private fun setLong(key: String?, value: Long) {
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }
    private fun setBoolean(key: String?, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
    
    private val datastore: Flow<Preferences>
        get() = context.dataStore.data
    
    // Preferences Implementation
    
    override val themePreference: DataStorePreference<UiMode>
        get() = object : DataStorePreference<UiMode> {
            override fun getFlow(): Flow<UiMode> =
                datastore.map { it[THEME].asUiMode() }
            
            override suspend fun set(value: UiMode) {
                context.dataStore.edit { it[THEME] = value.name }
            }
        }
    
    override var permissionsPreference: String?
        get() = preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        set(value) = setString(PREFERENCE_PERMS, value)
    
    override val listeningWhitelist: DataStorePreference<List<String>>
        get() = object: DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> =
                datastore.map { prefs ->
                    prefs[LISTENING_WHITELIST].asStringList()
                }
    
            override suspend fun set(value: List<String>) {
                context.dataStore.edit { prefs ->
                    prefs[LISTENING_WHITELIST] = gson.toJson(value)
                }
            }
    
            override suspend fun getAndUpdate(update: (List<String>) -> List<String>) {
                context.dataStore.updateData {
                    val updatedValue = update(it[LISTENING_WHITELIST].asStringList())
                    val mutablePrefs = it.toMutablePreferences()
                    mutablePrefs[LISTENING_WHITELIST] = gson.toJson(updatedValue)
                    return@updateData mutablePrefs
                }
            }
        }

    override val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(context.packageName)
        }
    
    override val isListeningAllowed: DataStorePreference<Boolean>
        get() = object: DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[IS_LISTENING_ALLOWED] ?: true
                }
            
            override suspend fun set(value: Boolean) {
                context.dataStore.edit { prefs ->
                    prefs[IS_LISTENING_ALLOWED] = value
                }
            }
        }
    
    override val shouldListenNewPlayers: DataStorePreference<Boolean>
        get() = object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> =
                datastore.map { prefs ->
                    prefs[SHOULD_LISTEN_NEW_PLAYERS] ?: true
                }
    
            override suspend fun set(value: Boolean) {
                context.dataStore.edit { prefs ->
                    prefs[SHOULD_LISTEN_NEW_PLAYERS] = value
                }
            }
        }
    
    override val listeningApps: DataStorePreference<List<String>>
        get() = object: DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> =
                datastore.map { prefs ->
                    prefs[LISTENING_APPS].asStringList()
                }
            
            override suspend fun set(value: List<String>) {
                context.dataStore.edit { prefs ->
                    prefs[LISTENING_APPS] = gson.toJson(value)
                }
            }
    
            override suspend fun getAndUpdate(update: (List<String>) -> List<String>) {
                context.dataStore.updateData {
                    val updatedValue = update(it[LISTENING_APPS].asStringList())
                    val mutablePrefs = it.toMutablePreferences()
                    mutablePrefs[LISTENING_APPS] = gson.toJson(updatedValue)
                    return@updateData mutablePrefs
                }
            }
        }

    override val version: String
        get() = try {
            context.packageManager?.getPackageInfo(context.packageName, 0)!!.versionName
        }
        catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }

    override var onboardingCompleted: Boolean
        get() = preferences.getBoolean(ONBOARDING, false)
        set(value) = setBoolean(ONBOARDING, value)
    
    override suspend fun logoutUser() = withContext(Dispatchers.IO) {
        val editor = preferences.edit()
        editor.remove(REFRESH_TOKEN)
        editor.remove(USERNAME)
        editor.apply()
        lbAccessToken.set("")
    }
    
    override var currentPlayable : Playable?
        get() = preferences.getString(CURRENT_PLAYABLE, "")?.let {
            if (it.isBlank()) null else
                TypeConverter.playableFromJSON(it)
        }
        set(value) {
            value?.let {
                setString(CURRENT_PLAYABLE, TypeConverter.playableToJSON(it))
            }
        }

    /* Login Preferences */
    
    override fun getLoginStatusFlow(): Flow<Int> =
         lbAccessToken.getFlow().map { token ->
             if (token.isNotEmpty())
                 STATUS_LOGGED_IN
             else
                 STATUS_LOGGED_OUT
         }.distinctUntilChanged()
    
    override suspend fun isUserLoggedIn() : Boolean =
        lbAccessToken.get().isNotEmpty()
    
    override val lbAccessToken: DataStorePreference<String>
        get() = object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
                }
    
            override suspend fun set(value: String) {
                context.dataStore.edit { prefs ->
                    prefs[PreferenceKeys.LB_ACCESS_TOKEN] = value
                }
            }
    
        }
    
    override val username: DataStorePreference<String>
        get() = object: DataStorePreference<String> {
            override fun getFlow(): Flow<String> =
                datastore.map { prefs ->
                    prefs[PreferenceKeys.USERNAME] ?: ""
                }
            
            override suspend fun set(value: String) {
                context.dataStore.edit { prefs ->
                    prefs[PreferenceKeys.USERNAME] = value ?: ""
                }
            }
    
        }
    
    override var linkedServices: List<LinkedService>
        get() {
            val jsonString = preferences.getString(LINKED_SERVICES, "")
            val type = object : TypeToken<List<LinkedService>>() {}.type
            return gson.fromJson(jsonString, type) ?: emptyList()
        }
        set(value) {
            val jsonString = gson.toJson(value)
            setString(LINKED_SERVICES, jsonString)
        }

    override val refreshToken: String?
        get() = preferences.getString(REFRESH_TOKEN, "")
    
    /* BrainzPlayer Preferences */
    
    override var albumsOnDevice: Boolean
        get() = preferences.getBoolean(PREFERENCE_ALBUMS_ON_DEVICE, true)
        set(value) = setBoolean(PREFERENCE_ALBUMS_ON_DEVICE, value)
    
    override var songsOnDevice: Boolean
        get() = preferences.getBoolean(PREFERENCE_SONGS_ON_DEVICE, true)
        set(value) = setBoolean(PREFERENCE_SONGS_ON_DEVICE, value)
}