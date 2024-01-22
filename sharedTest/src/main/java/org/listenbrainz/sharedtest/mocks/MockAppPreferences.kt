package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.DataStorePreference
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
    
    override val themePreference: DataStorePreference<UiMode> =
        object : DataStorePreference<UiMode> {
            override fun getFlow(): Flow<UiMode> = flow { emit(UiMode.FOLLOW_SYSTEM) }
            
            override suspend fun set(value: UiMode) {
                TODO("Not yet implemented")
            }
        }
    
    override val listeningWhitelist: DataStorePreference<List<String>> =
        object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> = flow {}
            
            override suspend fun set(value: List<String>) {
                TODO("Not yet implemented")
            }
        }
    
    override val listeningApps: DataStorePreference<List<String>> =
        object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> = flow {}
        
            override suspend fun set(value: List<String>) {
                TODO("Not yet implemented")
            }
        }
    
    override val lbAccessToken: DataStorePreference<String> =
        object : DataStorePreference<String> {
        override fun getFlow(): Flow<String> = flow {
                emit(testAccessToken)
            }
    
        override suspend fun set(value: String) {
            TODO("Not yet implemented")
        }
    }
    
    override val username: DataStorePreference<String> =
        object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> = flow {
                emit(testUsername)
            }
        
            override suspend fun set(value: String) {
                TODO("Not yet implemented")
            }
        }
    
    override val isListeningAllowed: DataStorePreference<Boolean> =
        object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> = flow {}
        
            override suspend fun set(value: Boolean) {
                TODO("Not yet implemented")
            }
        }
    
    override val shouldListenNewPlayers: DataStorePreference<Boolean> =
        object : DataStorePreference<Boolean> {
            override fun getFlow(): Flow<Boolean> = flow {}
        
            override suspend fun set(value: Boolean) {
                TODO("Not yet implemented")
            }
        }
    
    
    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }

    override fun getLoginStatusFlow(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }
}