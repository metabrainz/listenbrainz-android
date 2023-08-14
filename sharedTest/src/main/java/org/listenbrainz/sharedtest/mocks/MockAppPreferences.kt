package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAccessToken
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

/*
    For every new preference, add default value of the concerned shared
    preference as default value here.
*/
class MockAppPreferences(
    override val themePreference: String? = "Use device theme",
    override var permissionsPreference: String? = PermissionStatus.NOT_REQUESTED.name,
    override var onboardingCompleted: Boolean = false,
    override var currentPlayable: Playable? = null,
    override var username: String? = testUsername,
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
    
    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }
    
    override suspend fun getLbAccessToken(): String = testAccessToken
    
    override fun getLbAccessTokenFlow(): Flow<String> = flow {
        TODO("Not yet implemented")
    }
    
    override suspend fun setLbAccessToken(value: String) {
        TODO("Not yet implemented")
    }

    override var submitListens: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getLoginStatus(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
}