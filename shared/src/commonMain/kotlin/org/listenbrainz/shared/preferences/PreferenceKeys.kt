package org.listenbrainz.shared.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Preference keys shared across platforms.
 * These keys can be used to read/write values in DataStore.
 */
object PreferenceKeys {
    // Authentication
    val LB_ACCESS_TOKEN = stringPreferencesKey("lb_access_token")
    val USERNAME = stringPreferencesKey("username")
    
    // Listening preferences
    val LISTENING_BLACKLIST = stringPreferencesKey("listening_blacklist")
    val LISTENING_WHITELIST = stringPreferencesKey("listening_whitelist")
    val LISTENING_APPS = stringPreferencesKey("listening_apps")
    val IS_LISTENING_ALLOWED = booleanPreferencesKey("submit_listens")
    val SHOULD_LISTEN_NEW_PLAYERS = booleanPreferencesKey("listen_new_players")
    
    // UI preferences
    val THEME = stringPreferencesKey("app_theme")
    
    // Permissions
    val PERMISSIONS_REQUESTED = stringPreferencesKey("requested_permissions")
    
    // Login consent
    val CONSENT_SCREEN_CACHE = stringPreferencesKey("login_consent_screen_cache")
    
    // Install source
    val INSTALL_SOURCE = stringPreferencesKey("install_source")
    
    // App launch tracking
    val APP_LAUNCH_COUNT = stringPreferencesKey("app_launch_count")
    val LAST_VERSION_CHECK_LAUNCH_COUNT = stringPreferencesKey("last_version_check_launch_count")
    val LAST_UPDATE_PROMPT_LAUNCH_COUNT = stringPreferencesKey("last_update_prompt_launch_count")

    // BrainzPlayer
    val ALBUMS_ON_DEVICE = booleanPreferencesKey("PREFERENCE_ALBUMS_ON_DEVICE")
    val SONGS_ON_DEVICE = booleanPreferencesKey("PREFERENCE_SONGS_ON_DEVICE")
    val CURRENT_PLAYABLE = stringPreferencesKey("CURRENT_PLAYABLE")

    // Downloads
    val GITHUB_DOWNLOAD_ID = longPreferencesKey("download_id")

    // Onboarding
    val ONBOARDING = booleanPreferencesKey("onboarding_lb_updated")

    // Services
    val REFESH_TOKEN = stringPreferencesKey("refresh_token")
    val LINKED_SERVICES = stringPreferencesKey("LINKED_SERVICES")

    val PREFERENCE_NAV_ORDER = stringPreferencesKey("bottom_nav_order")
}
