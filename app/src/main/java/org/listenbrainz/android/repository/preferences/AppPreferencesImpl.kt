package org.listenbrainz.android.repository.preferences

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.model.UiMode.Companion.asUiMode
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.LISTENING_APPS
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys.THEME
import org.listenbrainz.android.util.Constants
import org.listenbrainz.android.util.Constants.ONBOARDING
import org.listenbrainz.android.util.Constants.Strings.CURRENT_PLAYABLE
import org.listenbrainz.android.util.Constants.Strings.LB_ACCESS_TOKEN
import org.listenbrainz.android.util.Constants.Strings.LINKED_SERVICES
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_ALBUMS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_APPS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_BLACKLIST
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

class AppPreferencesImpl(private val context : Context): AppPreferences {
    
    companion object {
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
                        PREFERENCE_LISTENING_BLACKLIST,
                        PREFERENCE_SYSTEM_THEME,
                        PREFERENCE_LISTENING_APPS
                    )
                ))
            }
        )
    
        private object PreferenceKeys {
            val LB_ACCESS_TOKEN = stringPreferencesKey(Constants.Strings.LB_ACCESS_TOKEN)
            val USERNAME = stringPreferencesKey(Constants.Strings.USERNAME)
            val LISTENING_BLACKLIST = stringPreferencesKey(PREFERENCE_LISTENING_BLACKLIST)
            val THEME = stringPreferencesKey(PREFERENCE_SYSTEM_THEME)
            val LISTENING_APPS = stringPreferencesKey(PREFERENCE_LISTENING_APPS)
        }
    }
    
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
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
    
    override suspend fun themePreference(): UiMode =
        datastore.first()[THEME].asUiMode()
    
    override fun themePreferenceFlow(): Flow<UiMode> =
        datastore.map { it[THEME].asUiMode() }
    
    override suspend fun setThemePreference(value: UiMode) {
        context.dataStore.edit { it[THEME] = value.name }
    }
    
    override var permissionsPreference: String?
        get() = preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        set(value) = setString(PREFERENCE_PERMS, value)
    
    override suspend fun getListeningBlacklist(): List<String> {
        return gson.fromJson(
            datastore.firstOrNull()?.get(PreferenceKeys.LISTENING_BLACKLIST) ?: "",
            object : TypeToken<List<String>>() {}.type
        ) ?: listOf()
    }
    
    
    override fun getListeningBlacklistFlow(): Flow<List<String>> =
        datastore.map { prefs ->
            gson.fromJson(
                prefs[PreferenceKeys.LISTENING_BLACKLIST] ?: "",
                object : TypeToken<List<String>>() {}.type
            ) ?: listOf()
        }
    
    override suspend fun setListeningBlacklist(value: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.LISTENING_BLACKLIST] = gson.toJson(value)
        }
    }

    override val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(context.packageName)
        }
    
    override var submitListens: Boolean
        get() = preferences.getBoolean(PREFERENCE_SUBMIT_LISTENS, true)
        set(value) { setBoolean(PREFERENCE_SUBMIT_LISTENS, value) }
    
    override suspend fun getListeningApps(): List<String> =
        gson.fromJson(
            datastore.firstOrNull()?.get(LISTENING_APPS) ?: "",
            object : TypeToken<List<String>>() {}.type
        ) ?: listOf()
    
    
    override fun getListeningAppsFlow(): Flow<List<String>> =
        datastore.map {
            gson.fromJson(
                datastore.firstOrNull()?.get(LISTENING_APPS) ?: "",
                object : TypeToken<List<String>>() {}.type
            ) ?: listOf()
        }
    
    override suspend fun setListeningApps(value: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[LISTENING_APPS] = gson.toJson(value)
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
        setLbAccessToken("")
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
         getLbAccessTokenFlow().map { token ->
             if (token.isNotEmpty())
                 STATUS_LOGGED_IN
             else
                 STATUS_LOGGED_OUT
         }.distinctUntilChanged()
    
    override suspend fun isUserLoggedIn() : Boolean =
        getLbAccessToken().isNotEmpty()
    
    override suspend fun getLbAccessToken(): String =
        datastore.firstOrNull()?.get(PreferenceKeys.LB_ACCESS_TOKEN) ?: ""
    
    override fun getLbAccessTokenFlow(): Flow<String> =
        datastore.map { prefs ->
            prefs[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
        }
    
    override suspend fun setLbAccessToken(value: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.LB_ACCESS_TOKEN] = value
        }
    }

    override fun getUsernameFlow(): Flow<String> =
        datastore.map { prefs ->
            prefs[PreferenceKeys.USERNAME] ?: ""
        }
    
    override suspend fun getUsername(): String =
        datastore.firstOrNull()?.get(PreferenceKeys.USERNAME) ?: ""
    
    override suspend fun setUsername(value: String?): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.USERNAME] = value ?: ""
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