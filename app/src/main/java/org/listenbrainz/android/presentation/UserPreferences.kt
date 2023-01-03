package org.listenbrainz.android.presentation

import androidx.preference.PreferenceManager
import org.listenbrainz.android.App

object UserPreferences {
    const val PREFERENCE_LISTENBRAINZ_TOKEN = "listenbrainz_user_token"
    const val PREFERENCE_LISTENING_ENABLED = "listening_enabled"
    private const val PREFERENCE_LISTENING_SPOTIFY = "listening_spotify_enabled"
    private const val PREFERENCE_SYSTEM_LANGUAGE = "use_english"
    const val PREFERENCE_SYSTEM_THEME = "app_theme"
    private const val PREFERENCE_ONBOARDING = "onboarding"
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
    val preferenceListeningSpotifyEnabled = preferences.getBoolean(PREFERENCE_LISTENING_SPOTIFY, false)
}