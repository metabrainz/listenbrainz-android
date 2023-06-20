package org.listenbrainz.android.repository.preferences

import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.util.LinkedService

interface AppPreferences {
    val themePreference : String?
    
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

    /** Blacklist for ListenService.*/
    var listeningBlacklist: List<String>
    
    /** Music Apps in users device registered by listenService.*/
    var listeningApps: List<String>

    var onboardingCompleted: Boolean

    fun saveOAuthToken(token: AccessToken)
    fun saveUserInfo(userInfo: UserInfo)
    fun logoutUser()

    val version: String
    
    var currentPlayable : Playable?
    
    /* Login related preferences */
    
    val loginStatus: Int
    /****ListenBrainz User Token:** User has to manually fill this token.*/
    var lbAccessToken: String?
    var username: String?
    val refreshToken: String?
    var linkedServices: List<LinkedService>

    val isNotificationServiceAllowed: Boolean
    
    /* BrainzPlayer Preferences */
    
    /** Used to tell the user that they don't have any albums on their device. */
    var albumsOnDevice: Boolean
    
    /** Used to tell the user that they don't have any songs on their device. */
    var songsOnDevice: Boolean
}