package org.listenbrainz.android.ui.screens.onboarding.introduction

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.listenbrainz.android.ui.components.DiagonalCutShape
import org.listenbrainz.android.ui.components.OnboardingBlobs
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_yellow
import org.listenbrainz.android.ui.theme.onboardingGradient

fun createSlideTransition(
    enterAnimDurationMs: Int = 300,
    exitAnimDurationMs: Int = 300,
    fadeInDurationMs: Int = 300,
    fadeOutDurationMs: Int = 300
): ContentTransform = ContentTransform(
    targetContentEnter = slideInVertically(animationSpec = tween(enterAnimDurationMs)) { height -> height } +
        fadeIn(animationSpec = tween(fadeInDurationMs)),
    initialContentExit = slideOutVertically(animationSpec = tween(exitAnimDurationMs)) { height -> -height } +
        fadeOut(animationSpec = tween(fadeOutDurationMs)),
    sizeTransform = SizeTransform(clip = false)
)


@Composable
fun IntroductionScreens(onOnboardingComplete: () -> Unit) {
    val screenCount = IntroScreenDataEnum.entries.size
    var currentScreen by rememberSaveable { mutableIntStateOf(1) }
    BackHandler(
        enabled = currentScreen != 1,
    ) {
        if (currentScreen > 1) {
            currentScreen--
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen > screenCount) {
            onOnboardingComplete()
        }
    }

    if (currentScreen <= screenCount) {
        IntroductionScreenUI(currentScreen, onClickNext = {
            if (currentScreen == screenCount) {
                onOnboardingComplete()
            } else {
                currentScreen++
            }},
            onSlide = {
                currentScreen = it
            })
    }
}

@Composable
private fun IntroductionScreenUI(screenNumber: Int, onClickNext: () -> Unit, onSlide: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val data = IntroScreenDataEnum.Companion.getScreenData(screenNumber)
    val pagerState = rememberPagerState(initialPage = screenNumber - 1, pageCount = {
        IntroScreenDataEnum.entries.size
    })
    val scrollState = rememberScrollState()
    LaunchedEffect(screenNumber) {
        if( pagerState.currentPage != screenNumber - 1) {
            delay(100)
            pagerState.animateScrollToPage(screenNumber - 1,
                animationSpec = spring(stiffness = Spring.StiffnessLow))
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != screenNumber - 1) {
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSlide(pagerState.currentPage + 1)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = onboardingGradient)
            .statusBarsPadding(),
    ) {
        if (screenNumber != 1) {
            OnboardingBackButton(modifier = Modifier
                .padding(top = 8.dp, start = 8.dp))
        }
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
                highlight = data.highlight,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedYellowButton(
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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -200f
                }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center),
                    painter = painterResource(IntroScreenDataEnum.entries[it].res),
                    contentDescription = null,
                )
            }

        }
    }
}


@Composable
fun OnboardingTitleAndSubtitle(
    title: String,
    highlight: String?,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val annotatedSubtitle = buildAnnotatedString {
        append(subtitle)
        highlight?.let {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = ListenBrainzTheme.colorScheme.text
                )
            ) {
                append(it)
            }
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = title,
            transitionSpec = { createSlideTransition() },
        ) { targetTitle ->
            Text(
                targetTitle,
                color = ListenBrainzTheme.colorScheme.listenText,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(18.dp))

        AnimatedContent(
            targetState = annotatedSubtitle,
            transitionSpec = { createSlideTransition(
                enterAnimDurationMs = 350,
                exitAnimDurationMs = 300,
                fadeInDurationMs = 350,
                fadeOutDurationMs = 300
            ) },
        ) { targetSubtitle ->
            Text(
                targetSubtitle,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(0.9f),
            )
        }
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

@Composable
fun OnboardingBackButton(modifier: Modifier = Modifier, onBackPress: (() -> Unit)? = null) {
    val haptic = LocalHapticFeedback.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    IconButton(
        modifier = modifier,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (onBackPress != null) onBackPress()
            else backDispatcher?.onBackPressed()
        },
        colors = IconButtonDefaults.iconButtonColors(
            //Suitable with the background
            containerColor = Color.White
        )
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "Back button",
            tint = lb_purple,
        )
    }
}

@Composable
private fun AnimatedYellowButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.shadow(4.dp, ListenBrainzTheme.shapes.listenCardSmall),
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = lb_yellow
        )
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = { createSlideTransition(
                enterAnimDurationMs = 250,
                exitAnimDurationMs = 200,
                fadeInDurationMs = 200,
                fadeOutDurationMs = 200
            ) },
            label = "button-text-animation"
        ) { targetText ->
            Text(
                targetText,
                color = ListenBrainzTheme.colorScheme.text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreen2Preview() {
    ListenBrainzTheme {
        IntroductionScreenUI(2, {}) {}
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreen1Preview() {
    ListenBrainzTheme {
        IntroductionScreenUI(1, {}) {}
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IntroductionScreen3Preview() {
    ListenBrainzTheme {
        IntroductionScreenUI(3, {}) {}
    }
}
