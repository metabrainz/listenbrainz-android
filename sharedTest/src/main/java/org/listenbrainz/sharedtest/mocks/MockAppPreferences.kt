package org.listenbrainz.sharedtest.mocks

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.model.Playable
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.preferences.AppPreferencesImpl.Companion.PreferenceKeys
import org.listenbrainz.android.repository.preferences.ProtoDataStore
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_IN
import org.listenbrainz.android.util.DataStoreSerializers.stringListSerializer
import org.listenbrainz.android.util.DataStoreSerializers.themeSerializer
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testAccessToken
import org.listenbrainz.sharedtest.utils.EntityTestUtils.testUsername

private val mockDataStore: DataStore<Preferences> = object : DataStore<Preferences> {
    override val data: Flow<Preferences>
        get() = flow {}
    
    /** Should NOT be called. Always override [ProtoDataStore.DataStorePreference.getAndUpdate] in mock.*/
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return transform(data.first())
    }
}

/**
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
): ProtoDataStore(dataStore = mockDataStore), AppPreferences {
    
    override val themePreference: DataStorePreference<UiMode, String> =
        object: DataStorePreference<UiMode, String>(
            key = PreferenceKeys.THEME,
            serializer = themeSerializer
        ) {}
    
    override val listeningWhitelist: DataStorePreference<List<String>, String> =
        object : DataStorePreference<List<String>, String>(
            key = PreferenceKeys.LISTENING_WHITELIST,
            serializer = stringListSerializer.apply {  }
        ) {}
    
    override val listeningApps: DataStorePreference<List<String>, String> =
        object : DataStorePreference<List<String>, String>(
            key = PreferenceKeys.LISTENING_APPS,
            serializer = stringListSerializer
        ) {}
    
    override val lbAccessToken: PrimitiveDataStorePreference<String> =
        object : PrimitiveDataStorePreference<String>(
            key = PreferenceKeys.LB_ACCESS_TOKEN,
            defaultValue = testAccessToken
        ) {}
    
    override val username: PrimitiveDataStorePreference<String> =
        object : PrimitiveDataStorePreference<String>(
            key = PreferenceKeys.USERNAME,
            defaultValue = testUsername
        ) {}
    
    override val isListeningAllowed: PrimitiveDataStorePreference<Boolean> =
        object : PrimitiveDataStorePreference<Boolean>(
            key = PreferenceKeys.IS_LISTENING_ALLOWED,
            defaultValue = true
        ) {}
    
    override val shouldListenNewPlayers: PrimitiveDataStorePreference<Boolean> =
        object : PrimitiveDataStorePreference<Boolean> (
            key = PreferenceKeys.SHOULD_LISTEN_NEW_PLAYERS,
            defaultValue = true
        ) {}
    
    
    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }

    override fun getLoginStatusFlow(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
    
    override suspend fun isUserLoggedIn(): Boolean = true
}