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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
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
import javax.inject.Singleton

@Singleton
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
                    setOf(LB_ACCESS_TOKEN, /*USERNAME*/)
                ))
            }
        )
    
        private object PreferenceKeys {
            val LB_ACCESS_TOKEN = stringPreferencesKey(Constants.Strings.LB_ACCESS_TOKEN)
            //val USERNAME = stringPreferencesKey(Constants.Strings.USERNAME)
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
    
    // Preferences Implementation
    override val themePreference: String?
        get() = preferences.getString(PREFERENCE_SYSTEM_THEME, "Use device theme")
    
    override var permissionsPreference: String?
        get() = preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        set(value) = setString(PREFERENCE_PERMS, value)
    
    override var listeningBlacklist: List<String>
        get() {
            val jsonString = preferences.getString(PREFERENCE_LISTENING_BLACKLIST, "")
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(jsonString, type) ?: listOf()
        }
        set(value) {
            val jsonString = gson.toJson(value)
            setString(PREFERENCE_LISTENING_BLACKLIST, jsonString)
        }

    override val isNotificationServiceAllowed: Boolean
        get() {
            val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return listeners != null && listeners.contains(context.packageName)
        }
    override var submitListens: Boolean
        get() = preferences.getBoolean(PREFERENCE_SUBMIT_LISTENS, true)
        set(value) { setBoolean(PREFERENCE_SUBMIT_LISTENS, value) }

    override var listeningApps: List<String>    // No need to use Set here
        get() {
            val jsonString = preferences.getString(PREFERENCE_LISTENING_APPS, "")
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(jsonString, type) ?: listOf()
        }
        set(value) {
            val jsonString = gson.toJson(value)
            setString(PREFERENCE_LISTENING_APPS, jsonString)
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
    
    override fun saveOAuthToken(token: AccessToken) {
        val editor = preferences.edit()
        editor.putString(REFRESH_TOKEN, token.refreshToken)
        editor.apply()
    }
    
    override fun saveUserInfo(userInfo: UserInfo) {
        val editor = preferences.edit()
        editor.putString(USERNAME, userInfo.username)
        editor.apply()
    }

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
    
    override fun getLoginStatus(): Flow<Int> =
         getLbAccessTokenFlow().map { token ->
             if (token.isNotEmpty())
                 STATUS_LOGGED_IN
             else
                 STATUS_LOGGED_OUT
         }.distinctUntilChanged()
    
    
    override suspend fun getLbAccessToken(): String =
        context.dataStore.data.first()[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
    
    override fun getLbAccessTokenFlow(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
        }
    
    
    override suspend fun setLbAccessToken(value: String): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.LB_ACCESS_TOKEN] = value
        }
    }

    /*override fun getUsernameFlow(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[PreferenceKeys.LB_ACCESS_TOKEN] ?: ""
        }
    
    
    override suspend fun getUsername(): String =
        context.dataStore.data.first()[PreferenceKeys.USERNAME] ?: ""
    
    override suspend fun setUsername(value: String): Unit = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.USERNAME] = value
        }
    }*/
    
    override var username: String?
        get() = preferences.getString(USERNAME, "")
        set(value) = setString(USERNAME, value)
    
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