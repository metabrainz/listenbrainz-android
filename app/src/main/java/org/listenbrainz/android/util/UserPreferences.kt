package org.listenbrainz.android.util

import androidx.preference.PreferenceManager
import org.listenbrainz.android.application.App

object UserPreferences {
    const val PREFERENCE_LISTENBRAINZ_TOKEN = "listenbrainz_user_token"
    const val PREFERENCE_LISTENING_BLACKLIST = "listening_blacklist"
    const val PREFERENCE_LISTENING_APPS = "listening_apps"
    const val PREFERENCE_LISTENING_ENABLED = "listening_enabled"
    const val PREFERENCE_LISTENING_SPOTIFY = "listening_spotify_enabled"
    const val PREFERENCE_SYSTEM_LANGUAGE = "use_english"
    const val PREFERENCE_SYSTEM_THEME = "app_theme"
    const val PREFERENCE_PERMS = "perms_code"
    const val PREFERENCE_ONBOARDING = "onboarding"
    const val PREFERENCE_ALBUMS_ON_DEVICE = "PREFERENCE_ALBUMS_ON_DEVICE"
    const val PREFERENCE_SONGS_ON_DEVICE = "PREFERENCE_SONGS_ON_DEVICE"
    enum class PermissionStatus{
        NOT_REQUESTED,
        GRANTED,
        DENIED_ONCE,
        DENIED_TWICE
    }
    
    // TODO: Start removal from here.
    private val preferences = PreferenceManager.getDefaultSharedPreferences(App.context!!)

    fun setOnBoardingCompleted() {
        val editor = preferences.edit()
        editor.putBoolean(PREFERENCE_ONBOARDING, true)
        editor.apply()
    }

    val systemLanguagePreference = preferences.getBoolean(PREFERENCE_SYSTEM_LANGUAGE, false)
    val themePreference = preferences.getString(PREFERENCE_SYSTEM_THEME, "Use device theme")

    val preferenceListenBrainzToken = preferences.getString(PREFERENCE_LISTENBRAINZ_TOKEN, null)
    var preferenceListeningEnabled: Boolean
        get() = preferences.getBoolean(PREFERENCE_LISTENING_ENABLED, false)
        set(value) {
            val editor = preferences.edit()
            editor.putBoolean(PREFERENCE_LISTENING_ENABLED, value)
            editor.apply()
        }
    /**
     *
     * [PermissionStatus.NOT_REQUESTED] -> permission not requested even once.
     *
     * [PermissionStatus.GRANTED]-> permission granted.
     *
     * [PermissionStatus.DENIED_ONCE] -> permission is denied once, user can be asked for permission again.
     *
     * [PermissionStatus.DENIED_TWICE] -> permission is denied twice and cannot be asked again. User need to go to settings to enable the permission.*/
    var permissionsPreference: String?
        get() {
            return preferences.getString(PREFERENCE_PERMS, PermissionStatus.NOT_REQUESTED.name)
        }
        set(value) {
            val editor = preferences.edit()
            editor.putString(PREFERENCE_PERMS, value)
            editor.apply()
        }
    
    val preferenceListeningSpotifyEnabled = preferences.getBoolean(PREFERENCE_LISTENING_SPOTIFY, false)
}