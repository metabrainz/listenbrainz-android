package org.listenbrainz.android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.createSlideTransition
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.onboardingGradient

@Composable
fun OnboardingScreenBackground(backStack: NavBackStack){
        AnimatedContent(targetState = backStack.last() in listOf(
            NavigationItem.OnboardingScreens.LoginScreen,
            NavigationItem.OnboardingScreens.LoginConsentScreen,
            NavigationItem.OnboardingScreens.PermissionScreen,
            NavigationItem.OnboardingScreens.ListeningAppScreen
        ),
            transitionSpec = {createSlideTransition(
                enterAnimDurationMs = 100
            )})
        {
            if(it) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(onboardingGradient)
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier) {
                        OnboardingBlobs()
                        Spacer(Modifier.height(50.dp))
                        OnboardingBlobs(isRotated = true)
                    }
                    OnboardingBackButton(
                        modifier = Modifier.align(Alignment.TopStart),
                    )
                }
            }
        }
}

@Preview
@Composable
fun OnboardingScreenBackgroundPreview() {
    ListenBrainzTheme {
        val backstack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginScreen)
        OnboardingScreenBackground(backstack)
    }
}