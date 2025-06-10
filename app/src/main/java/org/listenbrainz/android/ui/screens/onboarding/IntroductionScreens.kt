package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.components.DiagonalCutShape
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.screens.playlist.PlaylistButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.onboardingGradient
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.OnboardingBlobs

@Composable
fun IntroductionScreens(onOnboardingComplete: () -> Unit) {
    val screenCount = IntroScreenDataEnum.entries.size
    var currentScreen by rememberSaveable { mutableIntStateOf(1) }
    BackHandler(
        enabled = if(currentScreen == 1) false else true,
    ) {
        if( currentScreen > 1) {
            currentScreen--
        }
    }

    if (currentScreen <= screenCount) {
        IntroductionScreenUI(currentScreen) {
            if (currentScreen == screenCount) {
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
    val haptic = LocalHapticFeedback.current
    val data = IntroScreenDataEnum.getScreenData(screenNumber)
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = onboardingGradient)
            .statusBarsPadding()
        ,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(
                    ListenBrainzTheme.colorScheme.background,
                    shape = DiagonalCutShape(240f)
                )
        )
        OnboardingBlobs(modifier = Modifier.align(Alignment.Center))
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            IndicatorComposable(currentScreen = screenNumber - 1, size = 15)


                OnboardingTitleAndSubtitle(
                    title = data.title, subtitle = data.subtitle,
                    modifier = Modifier.fillMaxWidth()
                )
                OnboardingYellowButton(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    text = if (data.isLast) "Finish" else "Next",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onClickNext()
                    }
                )

        }


        Image(
            painter = painterResource(data.res), contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp)
                .graphicsLayer {
                    translationY = -200f
                })
    }
}


@Composable
fun OnboardingTitleAndSubtitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title, color = ListenBrainzTheme.colorScheme.listenText,
            fontSize = 22.sp, fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(18.dp))
        Text(subtitle, color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f))
    }
}


@Composable
private fun IndicatorComposable(
    modifier: Modifier = Modifier,
    noOfScreens: Int = IntroScreenDataEnum.entries.size,
    currentScreen: Int,
    size: Int = 50
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(noOfScreens) { index ->
            if (index == currentScreen) {
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .background(lb_orange, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size((size).dp)
                        .border(shape = CircleShape, color = lb_orange, width = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreen2Preview() {
    ListenBrainzTheme {
        IntroductionScreenUI(2) {}
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreen1Preview() {
    ListenBrainzTheme {
        IntroductionScreenUI(1) {}
    }
}
