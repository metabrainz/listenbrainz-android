package org.listenbrainz.android.data.repository

import org.listenbrainz.android.data.sources.api.entities.AccessToken
import org.listenbrainz.android.data.sources.api.entities.userdata.UserInfo
import org.listenbrainz.android.data.sources.brainzplayer.Playable
import org.listenbrainz.android.presentation.UserPreferences.PermissionStatus

interface AppPreferences {
    
    val systemLanguagePreference: Boolean
    
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
    
    var preferenceListeningEnabled: Boolean
    
    val preferenceListenBrainzToken : String?
    
    val onboardingPreference: Boolean
    
    val preferenceListeningSpotifyEnabled: Boolean
    
    fun saveOAuthToken(token: AccessToken)
    fun saveUserInfo(userInfo: UserInfo)
    fun logoutUser()
    
    var currentPlayable : Playable?
    
    /* Login related preferences */
    val loginStatus: Int
    val accessToken: String?
    val username: String?
    val refreshToken: String?
}