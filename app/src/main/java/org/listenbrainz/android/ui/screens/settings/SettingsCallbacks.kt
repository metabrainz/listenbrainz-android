package org.listenbrainz.android.ui.screens.settings

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import org.listenbrainz.android.ui.navigation.TopBarActions

@Immutable
data class SettingsCallbacks(
    val onOnboardingRequest: ()-> Unit,
    val onLoginRequest: ()-> Unit,
    val logout: () -> Unit,
    val getVersion: () -> String,
    val fetchLinkedServices: () -> Unit,
    val getPackageIcon: (String) -> Drawable?,
    val getPackageLabel: (String) -> String,
    val setWhitelist: (List<String>) -> Unit,
    val checkForUpdates: () -> Unit
)

@Immutable
data class SettingsCallbacksToHomeScreen(
    val checkForUpdates: () -> Unit,
    val topBarActions: TopBarActions,
    val onLoginRequest: () -> Unit,
    val onOnboardingRequest: () -> Unit
)