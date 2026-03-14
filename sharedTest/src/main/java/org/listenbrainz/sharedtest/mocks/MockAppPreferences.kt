package org.listenbrainz.sharedtest.mocks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.listenbrainz.shared.model.InstallSource
import org.listenbrainz.shared.model.Playable
import org.listenbrainz.shared.model.UiMode
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.shared.BottomNavDefaults
import org.listenbrainz.shared.model.AppNavigationItem
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.preferences.DataStorePreference
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAccessToken
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

/*
    For every new preference, add default value of the concerned shared
    preference as default value here.
*/
class MockAppPreferences : AppPreferences {

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
    override val onboardingCompleted: DataStorePreference<Boolean>
        get() = TODO("Not yet implemented")

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
    override val refreshToken: DataStorePreference<String?>
        get() = TODO("Not yet implemented")
    override val linkedServices: DataStorePreference<List<LinkedService>>
        get() = TODO("Not yet implemented")

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
    override val isNotificationServiceAllowed: Boolean
        get() = TODO("Not yet implemented")
    override val albumsOnDevice: DataStorePreference<Boolean>
        get() = TODO("Not yet implemented")
    override val songsOnDevice: DataStorePreference<Boolean>
        get() = TODO("Not yet implemented")

    override val requestedPermissionsList: DataStorePreference<List<String>> =
        object : DataStorePreference<List<String>> {
            override fun getFlow(): Flow<List<String>> = flow {  }

            override suspend fun set(value: List<String>) {
                TODO("Not yet implemented")
            }
        }

    override val consentScreenDataCache: DataStorePreference<String> =
        object : DataStorePreference<String> {
            override fun getFlow(): Flow<String> = flow {
                emit("")
            }

            override suspend fun set(value: String) {
                TODO("Not yet implemented")
            }
        }

    override val installSource: DataStorePreference<InstallSource>
        get() = TODO("Not yet implemented")

    override val appLaunchCount: DataStorePreference<Int>
        get() = TODO("Not yet implemented")

    override val lastVersionCheckLaunchCount: DataStorePreference<Int>
        get() = TODO("Not yet implemented")

    override val lastUpdatePromptLaunchCount: DataStorePreference<Int>
        get() = TODO("Not yet implemented")

    override val downloadId: DataStorePreference<Long>
        get() = TODO("Not yet implemented")

    override suspend fun logoutUser(): Boolean {
        TODO("Not yet implemented")
    }

    override val version: String
        get() = TODO("Not yet implemented")
    override val currentPlayable: DataStorePreference<Playable?>
        get() = TODO("Not yet implemented")

    override fun getLoginStatusFlow(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
    
    override suspend fun isUserLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }

    override val navBarOrder: DataStorePreference<List<AppNavigationItem>>
        get() = object: DataStorePreference<List<AppNavigationItem>> {
            override fun getFlow(): Flow<List<AppNavigationItem>> = flowOf(BottomNavDefaults.items())

            override suspend fun set(value: List<AppNavigationItem>) {
                TODO("Not yet implemented")
            }
        }
}