package org.listenbrainz.shared.ui.screens.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsCallbacks(
    val onOnboardingRequest: ()-> Unit,
    val onLoginRequest: ()-> Unit,
    val logout: () -> Unit,
    val getVersion: () -> String,
    val onDonateClick: () -> Unit,
    /** Null where the app can't update itself, which hides the option. */
    val checkForUpdates: (suspend () -> Boolean)?,
    /** Null where the navigation bar can't be reordered, which hides the option. */
    val onNavigationReorderClick: (() -> Unit)?
)

@Immutable
data class SettingsCallbacksToHomeScreen(
    val checkForUpdates: (suspend () -> Boolean)?,
    val onLoginRequest: () -> Unit,
    val onOnboardingRequest: () -> Unit
)