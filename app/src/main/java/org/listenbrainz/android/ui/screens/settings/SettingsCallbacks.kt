package org.listenbrainz.android.ui.screens.settings

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import org.listenbrainz.android.ui.navigation.TopBarActions
import org.listenbrainz.android.util.Resource

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
    val checkForUpdates: suspend () -> Boolean,
    val onReorderNavigationTabs: () -> Unit
)

@Immutable
data class SettingsCallbacksToHomeScreen(
    val checkForUpdates: suspend () -> Boolean,
    val topBarActions: TopBarActions,
    val onLoginRequest: () -> Unit,
    val onOnboardingRequest: () -> Unit,
    val navigateToCreateAccount : () -> Unit,
    val onReorderNavigationTabs: () -> Unit
)