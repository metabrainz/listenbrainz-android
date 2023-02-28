package org.listenbrainz.android.repository

import androidx.preference.PreferenceManager
import org.listenbrainz.android.application.App
import org.listenbrainz.android.util.TypeConverter
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_LISTENBRAINZ_TOKEN
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_LISTENING_ENABLED
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_LISTENING_SPOTIFY
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_ONBOARDING
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_PERMS
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_SYSTEM_LANGUAGE
import org.listenbrainz.android.util.UserPreferences.PREFERENCE_SYSTEM_THEME
import org.listenbrainz.android.util.UserPreferences.PermissionStatus
import org.listenbrainz.android.util.LBSharedPreferences

class AppPreferencesImpl: AppPreferences {
    
    private val preferences = PreferenceManager.getDefaultSharedPreferences(App.context!!)
    
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
    
    override val preferenceListenBrainzToken: String?
        get() = preferences.getString(PREFERENCE_LISTENBRAINZ_TOKEN, null)
    
    override var onboardingPreference: Boolean
        get() = preferences.getBoolean(PREFERENCE_ONBOARDING, false)
        set(value) = setBoolean(PREFERENCE_ONBOARDING, value)
    
    override val preferenceListeningSpotifyEnabled
        get() = preferences.getBoolean(PREFERENCE_LISTENING_SPOTIFY, false)
    
    
    override fun saveOAuthToken(token: AccessToken) {
        val editor = preferences.edit()
        editor.putString(LBSharedPreferences.ACCESS_TOKEN, token.accessToken)
        editor.putString(LBSharedPreferences.REFRESH_TOKEN, token.refreshToken)
        editor.apply()
    }
    
    override fun saveUserInfo(userInfo: UserInfo) {
        val editor = preferences.edit()
        editor.putString(LBSharedPreferences.USERNAME, userInfo.username)
        editor.apply()
    }
    
    override fun logoutUser() {
        val editor = preferences.edit()
        editor.remove(LBSharedPreferences.ACCESS_TOKEN)
        editor.remove(LBSharedPreferences.REFRESH_TOKEN)
        editor.remove(LBSharedPreferences.USERNAME)
        editor.apply()
    }
    
    override var currentPlayable : Playable?
        get() = preferences.getString(LBSharedPreferences.CURRENT_PLAYABLE, "")?.let {
            if (it.isBlank()) null else
                TypeConverter.playableFromJSON(it)
        }
        set(value) {
            value?.let {
                setString(LBSharedPreferences.CURRENT_PLAYABLE, TypeConverter.playableToJSON(it))
            }
        }
    
    override val loginStatus: Int
        get() {
            val accessToken = accessToken
            val username = username
            return if (accessToken!!.isNotEmpty() && username!!.isNotEmpty()) LBSharedPreferences.STATUS_LOGGED_IN else LBSharedPreferences.STATUS_LOGGED_OUT
        }
    
    override val accessToken: String?
        get() = preferences.getString(LBSharedPreferences.ACCESS_TOKEN, "")
    override val username: String?
        get() = preferences.getString(LBSharedPreferences.USERNAME, "")
    override val refreshToken: String?
        get() = preferences.getString(LBSharedPreferences.REFRESH_TOKEN, "")
    
}