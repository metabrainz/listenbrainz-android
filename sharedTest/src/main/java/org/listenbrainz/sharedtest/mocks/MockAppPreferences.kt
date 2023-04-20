package org.listenbrainz.sharedtest.mocks

import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.UserPreferences

/*
    For every new preference, add default value of the concerned shared
    preference as default value here.
*/
class MockAppPreferences(
    override val systemLanguagePreference: Boolean = false,
    override val themePreference: String? = "Use device theme",
    override var permissionsPreference: String? = UserPreferences.PermissionStatus.NOT_REQUESTED.name,
    override var preferenceListeningEnabled: Boolean = false,
    override val preferenceListenBrainzToken: String? = null,
    override val onboardingPreference: Boolean = false,
    override val preferenceListeningSpotifyEnabled: Boolean = false,
    override var currentPlayable: Playable? = null,
    override val loginStatus: Int = LBSharedPreferences.STATUS_LOGGED_OUT,
    override val mbAccessToken: String? = "",
    override val lbAccessToken: String? = "",
    override val username: String? = "",
    override val refreshToken: String? = "",
    override var albumsOnDevice: Boolean = true,
    override var songsOnDevice: Boolean = true,
    override var listeningBlacklist: List<String> = listOf(),
    override var listeningApps: List<String> = listOf()
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