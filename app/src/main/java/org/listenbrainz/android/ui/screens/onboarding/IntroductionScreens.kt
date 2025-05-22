package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.screens.playlist.PlaylistButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun IntroductionScreens(onOnboardingComplete: () -> Unit) {
    val screenCount = IntroScreenDataEnum.entries.size
    var currentScreen by rememberSaveable { mutableIntStateOf(0) }

    if (currentScreen < screenCount) {
        IntroductionScreenUI(currentScreen) {
            if (currentScreen == screenCount - 1) {
                onOnboardingComplete()
            } else {
                currentScreen++
            }
        }
    } else {
        onOnboardingComplete()
    }
}

@Composable
private fun IntroductionScreenUI(screenNumber: Int, onClickNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val data = IntroScreenDataEnum.getScreenData(screenNumber)
        Text(data.title, color = ListenBrainzTheme.colorScheme.listenText)
        Spacer(Modifier.height(16.dp))
        Text(data.subtitle, color = ListenBrainzTheme.colorScheme.text)
        Spacer(Modifier.height(16.dp))
        PlaylistButton(onClick = onClickNext) {
            Text(text = if (data.isLast) "Finish" else "Next")
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreenPreview() {
    ListenBrainzTheme {
        IntroductionScreenUI(2) {}
    }
}