package org.listenbrainz.sharedtest.mocks

/**
    For every new preference, add default value of the concerned shared
    preference as default value here.
*/
/*
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
): AppPreferences {
    
    override val themePreference: ComplexPreference<UiMode> =
        mockComplexPreference(UiMode.FOLLOW_SYSTEM)
    
    override val listeningWhitelist: ComplexPreference<List<String>> =
        mockComplexPreference(emptyList())
    
    override val listeningApps: ComplexPreference<List<String>> =
        mockComplexPreference(emptyList())
    
    override val lbAccessToken: PrimitivePreference<String> =
        mockPrimitivePreference(testAccessToken)
    
    override val username: PrimitivePreference<String> =
        mockPrimitivePreference(testUsername)
    
    override val isListeningAllowed: PrimitivePreference<Boolean> =
       mockPrimitivePreference(true)
    
    override val shouldListenNewPlayers: PrimitivePreference<Boolean> =
        mockPrimitivePreference(true)
    
    override suspend fun logoutUser() {
        TODO("Not yet implemented")
    }

    override fun getLoginStatusFlow(): Flow<Int> = flow {
        emit(STATUS_LOGGED_IN)
    }
    
    override suspend fun isUserLoggedIn(): Boolean = true
}*/
