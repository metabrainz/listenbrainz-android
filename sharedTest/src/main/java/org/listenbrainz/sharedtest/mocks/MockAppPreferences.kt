package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
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
    override var permissionsPreference: String? = PermissionStatus.NOT_REQUESTED.name,
    override var onboardingCompleted: Boolean = false,
    override var currentPlayable: Playable? = null,
    override val refreshToken: String? = "",
    override var albumsOnDevice: Boolean = true,
    override var songsOnDevice: Boolean = true,
    override val version: String = "",
    override val isNotificationServiceAllowed: Boolean = true,
    override var linkedServices: List<LinkedService> = listOf()
) : AppPreferences {
    override suspend fun themePreference(): UiMode {
        TODO("Not yet implemented")
    }
    
    override fun themePreferenceFlow(): Flow<UiMode> {
        TODO("Not yet implemented")
    }
    
    override suspend fun setThemePreference(value: UiMode) {
        TODO("Not yet implemented")
    }
    
    override suspend fun getListeningWhitelist(): List<String> {
        TODO("Not yet implemented")
    }
    
    override fun getListeningWhitelistFlow(): Flow<List<String>> {
        TODO("Not yet implemented")
    }
    
    override suspend fun setListeningWhitelist(value: List<String>) {
        TODO("Not yet implemented")
    }
    
    override suspend fun getListeningApps(): List<String> {
        TODO("Not yet implemented")
    }
    
    override fun getListeningAppsFlow(): Flow<List<String>> {
        TODO("Not yet implemented")
    }
    
    override suspend fun setListeningApps(value: List<String>) {
        TODO("Not yet implemented")
    }
    
    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }
    
    override suspend fun getLbAccessToken(): String = testAccessToken
    
    override fun getLbAccessTokenFlow(): Flow<String> = flow {
        emit(testAccessToken)
    }
    
    override suspend fun setLbAccessToken(value: String) {
        TODO("Not yet implemented")
    }
    
    override fun getUsernameFlow(): Flow<String> = MutableStateFlow(testUsername)
    
    override suspend fun getUsername(): String = testUsername
    
    override suspend fun setUsername(value: String?) {
        TODO("Not yet implemented")
    }
    
    override var submitListens: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getLoginStatusFlow(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }
}