package org.listenbrainz.android

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.listenbrainz.android.model.UiMode
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.isUiModeIsDark

fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setExplicitContent(
    uiMode: UiMode = UiMode.DARK,
    content: @Composable () -> Unit
) {
    isUiModeIsDark = when(uiMode) {
        UiMode.LIGHT -> mutableStateOf(false)
        UiMode.DARK -> mutableStateOf(true)
        UiMode.FOLLOW_SYSTEM -> mutableStateOf(null)
    }
    
    this.setContent {
        ListenBrainzTheme {
            content()
        }
    }
}
