package org.listenbrainz.android.repository

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.util.Constants.ONBOARDING
import org.listenbrainz.android.util.Constants.Strings.CURRENT_PLAYABLE
import org.listenbrainz.android.util.Constants.Strings.LB_ACCESS_TOKEN
import org.listenbrainz.android.util.Constants.Strings.MB_ACCESS_TOKEN
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_ALBUMS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_APPS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_BLACKLIST
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_ENABLED
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_LISTENING_SPOTIFY
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_PERMS
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SONGS_ON_DEVICE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_LANGUAGE
import org.listenbrainz.android.util.Constants.Strings.PREFERENCE_SYSTEM_THEME
import org.listenbrainz.android.util.Constants.Strings.REFRESH_TOKEN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.Constants.Strings.USERNAME
import org.listenbrainz.android.util.TypeConverter
import javax.inject.Singleton

@Singleton
class AppPreferencesImpl(private val context : Context): AppPreferences {
    
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
    
    override val systemLanguagePreference: Boolean
        get() = preferences.getBoolean(PREFERENCE_SYSTEM_LANGUAGE, false)
    
    override val themePreference: String?
        get() = preferences.getString(PREFERENCE_SYSTEM_THEME, "Use device theme")
    
    override var permissionsPreference: String?
        get() = preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        set(value) = setString(PREFERENCE_PERMS, value)
    
    override var preferenceListeningEnabled: Boolean
        get() = preferences.getBoolean(PREFERENCE_LISTENING_ENABLED, false)
        set(value) = setBoolean(PREFERENCE_LISTENING_ENABLED, value)
    
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
    
    override val preferenceListeningSpotifyEnabled
        get() = preferences.getBoolean(PREFERENCE_LISTENING_SPOTIFY, false)
    
    
    override fun saveOAuthToken(token: AccessToken) {
        val editor = preferences.edit()
        editor.putString(MB_ACCESS_TOKEN, token.accessToken)
        editor.putString(REFRESH_TOKEN, token.refreshToken)
        editor.apply()
    }
    
    override fun saveUserInfo(userInfo: UserInfo) {
        val editor = preferences.edit()
        editor.putString(USERNAME, userInfo.username)
        editor.apply()
    }
    
    override fun logoutUser() {
        val editor = preferences.edit()
        editor.remove(MB_ACCESS_TOKEN)
        editor.remove(REFRESH_TOKEN)
        editor.remove(USERNAME)
        editor.apply()
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
    
    override val loginStatus: Int
        get() {
            val accessToken = mbAccessToken
            val username = username
            return when {
                accessToken!!.isNotEmpty() && username!!.isNotEmpty() -> STATUS_LOGGED_IN
                else -> STATUS_LOGGED_OUT
            }
        }
    
    override val mbAccessToken: String?
        get() = preferences.getString(MB_ACCESS_TOKEN, "")
    override var lbAccessToken: String?
        get() = preferences.getString(LB_ACCESS_TOKEN, "")
        set(value) = setString(LB_ACCESS_TOKEN, value)

    override val username: String?
        get() = preferences.getString(USERNAME, "")
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