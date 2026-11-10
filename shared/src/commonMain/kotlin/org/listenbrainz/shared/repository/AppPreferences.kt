package org.listenbrainz.shared.repository

import kotlinx.coroutines.flow.Flow
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.model.InstallSource
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.preferences.DataStorePreference

interface AppPreferences {
    
    val themePreference: DataStorePreference<UiMode>

    /** List of permissions requested by the app atleast once. */
    val requestedPermissionsList: DataStorePreference<List<String>>

    /** Whitelist for ListenSubmissionService.*/
    val listeningWhitelist: DataStorePreference<List<String>>
    
    /** Music Apps in users device registered by listenService.*/
    val listeningApps: DataStorePreference<List<String>>

    val onboardingCompleted: DataStorePreference<Boolean>
    
    suspend fun logoutUser(): Boolean

    val version: String
    
    /* Login related preferences */
    fun getLoginStatusFlow(): Flow<Int>
    
    suspend fun isUserLoggedIn() : Boolean
    
    /****ListenBrainz User Token:** User has to manually fill this token.*/
    val lbAccessToken: DataStorePreference<String>
    
    val username: DataStorePreference<String>
    
    val refreshToken: DataStorePreference<String?>
    
    val linkedServices: DataStorePreference<List<LinkedService>>
    
    /** Default is true. */
    val isListeningAllowed: DataStorePreference<Boolean>
    
    /** Default is false. */
    val shouldListenNewPlayers: DataStorePreference<Boolean>

    val isNotificationServiceAllowed: Boolean


    val installSource: DataStorePreference<InstallSource>
    
    /** Current app launch count. Incremented on each app launch. */
    val appLaunchCount: DataStorePreference<Int>
    
    /** Launch count when version was last checked. */
    val lastVersionCheckLaunchCount: DataStorePreference<Int>
    
    /** Launch count when user was last prompted to update. */
    val lastUpdatePromptLaunchCount: DataStorePreference<Int>

    val downloadId: DataStorePreference<Long>

    val navBarOrder: DataStorePreference<List<AppNavigationItem>>
}
