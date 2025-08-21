package org.listenbrainz.android.repository.preferences

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.util.LinkedService

interface AppPreferences {
    
    val themePreference: DataStorePreference<UiMode>

    /** List of permissions requested by the app atleast once. */
    val requestedPermissionsList: DataStorePreference<List<String>>

    /** Whitelist for ListenSubmissionService.*/
    val listeningWhitelist: DataStorePreference<List<String>>
    
    /** Music Apps in users device registered by listenService.*/
    val listeningApps: DataStorePreference<List<String>>

    var onboardingCompleted: Boolean
    
    suspend fun logoutUser(): Boolean

    val version: String
    
    var currentPlayable : Playable?
    
    /* Login related preferences */
    fun getLoginStatusFlow(): Flow<Int>
    
    suspend fun isUserLoggedIn() : Boolean
    
    /****ListenBrainz User Token:** User has to manually fill this token.*/
    val lbAccessToken: DataStorePreference<String>
    
    val username: DataStorePreference<String>
    
    val refreshToken: String?
    
    var linkedServices: List<LinkedService>
    
    /** Default is true. */
    val isListeningAllowed: DataStorePreference<Boolean>
    
    /** Default is false. */
    val shouldListenNewPlayers: DataStorePreference<Boolean>

    val isNotificationServiceAllowed: Boolean
    
    /* BrainzPlayer Preferences */
    
    /** Used to tell the user that they don't have any albums on their device. */
    var albumsOnDevice: Boolean
    
    /** Used to tell the user that they don't have any songs on their device. */
    var songsOnDevice: Boolean

    /** Cache for Login Consent Screen Data */
    val consentScreenDataCache: DataStorePreference<String>

    val installSource: DataStorePreference<InstallSource>
    
    /** Current app launch count. Incremented on each app launch. */
    val appLaunchCount: DataStorePreference<Int>
    
    /** Launch count when version was last checked. */
    val lastVersionCheckLaunchCount: DataStorePreference<Int>
    
    /** Launch count when user was last prompted to update. */
    val lastUpdatePromptLaunchCount: DataStorePreference<Int>
}