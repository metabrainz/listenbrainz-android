package org.listenbrainz.android.ui.screens.settings

import org.listenbrainz.android.model.UiMode

data class PreferencesUiState(
    val isSpotifyLinked: Boolean = false,
    val username: String = "",
    val accessToken: String = "",
    val isNotificationServiceAllowed: Boolean = false,
    val listeningWhitelist: List<String> = emptyList(),
    val listeningApps: List<String> = emptyList(),
    val theme: UiMode = UiMode.FOLLOW_SYSTEM
)