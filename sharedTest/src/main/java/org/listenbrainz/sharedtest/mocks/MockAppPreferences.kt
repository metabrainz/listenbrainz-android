package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.LinkedService

/*
    For every new preference, add default value of the concerned shared
    preference as default value here.
*/
class MockAppPreferences(
    override val themePreference: String? = "Use device theme",
    override var permissionsPreference: String? = PermissionStatus.NOT_REQUESTED.name,
    override var onboardingCompleted: Boolean = false,
    override var currentPlayable: Playable? = null,
    override val loginStatus: Int = STATUS_LOGGED_OUT,
    override var lbAccessToken: String? = "",
    override var username: String? = "",
    override val refreshToken: String? = "",
    override var albumsOnDevice: Boolean = true,
    override var songsOnDevice: Boolean = true,
    override var listeningBlacklist: List<String> = listOf(),
    override var listeningApps: List<String> = listOf(),
    override val version: String = "",
    override val isNotificationServiceAllowed: Boolean = true,
    override var linkedServices: List<LinkedService> = listOf()
) : AppPreferences {
    
    override fun saveOAuthToken(token: AccessToken) {
        TODO("Not yet implemented")
    }
    
    override fun saveUserInfo(userInfo: UserInfo) {
        TODO("Not yet implemented")
    }
    
    override fun logoutUser() {
        TODO("Not yet implemented")
    }
}