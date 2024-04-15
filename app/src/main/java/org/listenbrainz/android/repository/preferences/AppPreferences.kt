package org.listenbrainz.android.repository.preferences

import com.jasjeet.typesafe_datastore.preferences.ComplexPreference
import com.jasjeet.typesafe_datastore.preferences.PrimitivePreference
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.util.LinkedService

interface AppPreferences {
    
    val themePreference: ComplexPreference<UiMode>
    
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

    /** Whitelist for ListenSubmissionService.*/
    val listeningWhitelist: ComplexPreference<List<String>>
    
    /** Music Apps in users device registered by listenService.*/
    val listeningApps: ComplexPreference<List<String>>

    var onboardingCompleted: Boolean
    
    suspend fun logoutUser()

    val version: String
    
    var currentPlayable : Playable?
    
    /* Login related preferences */
    fun getLoginStatusFlow(): Flow<Int>
    
    suspend fun isUserLoggedIn() : Boolean
    
    /****ListenBrainz User Token:** User has to manually fill this token.*/
    val lbAccessToken: PrimitivePreference<String>
    
    val username: PrimitivePreference<String>
    
    val refreshToken: String?
    
    var linkedServices: List<LinkedService>
    
    /** Default is true. */
    val isListeningAllowed: PrimitivePreference<Boolean>
    
    /** Default is true. */
    val shouldListenNewPlayers: PrimitivePreference<Boolean>

    val isNotificationServiceAllowed: Boolean
    
    /* BrainzPlayer Preferences */
    
    /** Used to tell the user that they don't have any albums on their device. */
    var albumsOnDevice: Boolean
    
    /** Used to tell the user that they don't have any songs on their device. */
    var songsOnDevice: Boolean
}