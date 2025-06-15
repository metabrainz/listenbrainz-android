package org.listenbrainz.android.ui.screens.onboarding.listeningApps

import android.content.res.Configuration
import androidx.compose.foundation.Image
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.components.OnboardingBlobs
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.onboardingGradient
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun ListeningAppSelectionScreen(
    dashBoardViewModel: DashBoardViewModel = hiltViewModel(),
    onClickNext: () -> Unit){
    val apps by dashBoardViewModel.listeningAppsFlow.collectAsState()
    val isListening by dashBoardViewModel.appPreferences.isListeningAllowed.getFlow().collectAsState(initial = true)
    val areNewPlayersEnabled by dashBoardViewModel.appPreferences.shouldListenNewPlayers.getFlow().collectAsState(initial = true)
    ListeningAppScreenLayout(
        apps = apps,
        isListening = isListening,
        areNewPlayersEnabled = areNewPlayersEnabled,
        onCheckedChange = dashBoardViewModel::onAppCheckChange,
        onListeningCheckChange = dashBoardViewModel::onListeningStatusChange,
        onEnabledPlayersCheckChange = dashBoardViewModel::onNewPlayersEnabledStatusChange
    ) {
        onClickNext()
    }
}

@Composable
fun ListeningAppScreenLayout(
    apps: List<AppInfo>,
    isListening: Boolean,
    areNewPlayersEnabled: Boolean,
    onCheckedChange: (Boolean, AppInfo)->Unit,
    onListeningCheckChange: (Boolean)->Unit,
    onEnabledPlayersCheckChange: (Boolean)->Unit,
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
                EnableListenSubmission(
                    apps = apps,
                    isListening = isListening,
                    areNewPlayersEnabled = areNewPlayersEnabled,
                    onCheckedChange = onCheckedChange,
                    onListeningCheckChange = onListeningCheckChange,
                    onEnabledPlayersCheckChange = onEnabledPlayersCheckChange,
                )
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
fun AppCard(
    appInfo: AppInfo,
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
            Image(modifier = Modifier.size(40.dp),
                bitmap = appInfo.icon.asImageBitmap(),
                contentDescription = "App icon")

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = appInfo.appName,
                style = MaterialTheme.typography.bodyLarge,
                color = ListenBrainzTheme.colorScheme.text,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = appInfo.isWhitelisted,
            onCheckedChange = if (enabled) onCheckedChange else { {} }
        )
    }
}


@Composable
fun EnableListenSubmission(
    apps: List<AppInfo>,
    isListening: Boolean,
    areNewPlayersEnabled: Boolean,
    onCheckedChange: (Boolean, AppInfo)->Unit,
    onListeningCheckChange: (Boolean)->Unit,
    onEnabledPlayersCheckChange: (Boolean)->Unit,
    modifier: Modifier = Modifier
) {
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

                Switch(
                    checked = isListening,
                    onCheckedChange = onListeningCheckChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose the apps you want ListenBrainz to track. This helps automatically submit your listens while ensuring accurate listening history",
                style = MaterialTheme.typography.bodyMedium,
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            apps.forEachIndexed {ind, app->
                AppCard(
                    appInfo = app,
                    onCheckedChange = {
                        onCheckedChange(it, app)
                    },
                    enabled = isListening
                )
                Spacer(modifier = Modifier.height(6.dp))
                if(apps.lastIndex != ind) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

            }

            Spacer(modifier = Modifier.height(24.dp))

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

                Switch(
                    checked = areNewPlayersEnabled,
                    onCheckedChange = if(isListening) onEnabledPlayersCheckChange else {{}}
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
            onClickNext = {},
            apps = emptyList(),
            isListening = true,
            areNewPlayersEnabled = true,
            onCheckedChange = {_,_->},
            onListeningCheckChange = {},
            onEnabledPlayersCheckChange = {}
        )
    }
}