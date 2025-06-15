package org.listenbrainz.android.ui.screens.onboarding.listeningApps

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.ui.components.LBSwitch
import org.listenbrainz.android.ui.components.OnboardingBlobs
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.onboardingGradient

@Composable
fun ListeningAppSelectionScreen(onClickNext: () -> Unit){
    ListeningAppScreenLayout {
        onClickNext()
    }
}

@Composable
fun ListeningAppScreenLayout(
    onClickNext: () -> Unit,
){
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = onboardingGradient)
    ) {

       Column(modifier = Modifier.graphicsLayer{
            translationY = 800f
        }) {
            OnboardingBlobs()
            Spacer(Modifier.height(50.dp))
            OnboardingBlobs(isRotated = true)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            item {
                OnboardingBackButton(
                    modifier = Modifier.graphicsLayer{
                        //Reverse the effect of padding on the back button
                        translationX = with(density){-24.dp.toPx()}
                    }
                )
                Spacer(Modifier.height(48.dp))
            }
            item {
                Text(
                    "Submit Listens",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Listen Submission tracks your music activity, helping you discover stats, trends, and personalized recommendations.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
                Spacer(Modifier.height(32.dp))
            }
            item {
                EnableListenSubmissionWithPlaceholders()
            }
            
            item{
                Spacer(Modifier.height(36.dp))
                OnboardingYellowButton(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    text = "Done",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onClickNext()
                    }
                )
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

// Alternative version using placeholders if you don't have the actual icons yet
@Composable
fun AppCardWithPlaceholder(
    appName: String,
    backgroundColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = ListenBrainzTheme.colorScheme.text,
                fontWeight = FontWeight.Medium
            )
        }

        LBSwitch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else { {} }
        )
    }
}


@Composable
fun EnableListenSubmissionWithPlaceholders(
    modifier: Modifier = Modifier
) {
    // State for the main toggle and individual app toggles
    var listenSubmissionEnabled by remember { mutableStateOf(true) }
    var youtubeEnabled by remember { mutableStateOf(false) }
    var spotifyEnabled by remember { mutableStateOf(false) }
    var youtubeMusicEnabled by remember { mutableStateOf(true) }
    var amazonMusicEnabled by remember { mutableStateOf(true) }
    var listenBrainzEnabled by remember { mutableStateOf(false) }
    var audiomackEnabled by remember { mutableStateOf(true) }
    var allowNewPlayersEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .background(
                ListenBrainzTheme.colorScheme.background.copy(alpha = 0.75f),
                shape = ListenBrainzTheme.shapes.listenCardSmall
            )
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Listen Submission",
                    style = MaterialTheme.typography.titleMedium,
                    color = ListenBrainzTheme.colorScheme.text,
                    fontWeight = FontWeight.Medium
                )

                LBSwitch(
                    checked = listenSubmissionEnabled,
                    onCheckedChange = { listenSubmissionEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description Text
            Text(
                text = "Choose the apps you want ListenBrainz to track. This helps automatically submit your listens while ensuring accurate listening history",
                style = MaterialTheme.typography.bodyMedium,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apps List with placeholder colors
            AppCardWithPlaceholder(
                appName = "Youtube",
                backgroundColor = Color(0xFFFF0000),
                checked = youtubeEnabled,
                onCheckedChange = { youtubeEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppCardWithPlaceholder(
                appName = "Spotify",
                backgroundColor = Color(0xFF1DB954),
                checked = spotifyEnabled,
                onCheckedChange = { spotifyEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppCardWithPlaceholder(
                appName = "Youtube Music",
                backgroundColor = Color(0xFFFF0000),
                checked = youtubeMusicEnabled,
                onCheckedChange = { youtubeMusicEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppCardWithPlaceholder(
                appName = "Amazon Music",
                backgroundColor = Color(0xFF00A8E1),
                checked = amazonMusicEnabled,
                onCheckedChange = { amazonMusicEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppCardWithPlaceholder(
                appName = "ListenBrainz",
                backgroundColor = Color(0xFF353070),
                checked = listenBrainzEnabled,
                onCheckedChange = { listenBrainzEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppCardWithPlaceholder(
                appName = "Audiomack",
                backgroundColor = Color(0xFF000000),
                checked = audiomackEnabled,
                onCheckedChange = { audiomackEnabled = it },
                enabled = listenSubmissionEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Section
            Text(
                text = "When a new music app is detected, automatically use it to submit listens.",
                style = MaterialTheme.typography.bodyMedium,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Allow new players",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ListenBrainzTheme.colorScheme.text,
                    fontWeight = FontWeight.Medium
                )

                LBSwitch(
                    checked = allowNewPlayersEnabled,
                    onCheckedChange = { allowNewPlayersEnabled = it }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ListeningAppLayoutPreview() {
    ListenBrainzTheme {
        ListeningAppScreenLayout(
            onClickNext = {}
        )
    }
}