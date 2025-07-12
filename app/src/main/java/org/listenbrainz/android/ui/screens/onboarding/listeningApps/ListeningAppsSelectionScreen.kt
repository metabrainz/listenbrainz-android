package org.listenbrainz.android.ui.screens.onboarding.listeningApps

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.R
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.components.Switch
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.introduction.createSlideTransition
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionCard
import org.listenbrainz.android.ui.screens.onboarding.permissions.PermissionEnum
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@Composable
fun ListeningAppSelectionScreen(
    dashBoardViewModel: DashBoardViewModel = hiltViewModel(),
    onClickNext: () -> Unit
) {
    val apps by dashBoardViewModel.listeningAppsFlow.collectAsState()
    val isListening by dashBoardViewModel.appPreferences.isListeningAllowed.getFlow()
        .collectAsState(initial = true)
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()

    val notificationPermission = permissions[PermissionEnum.READ_NOTIFICATIONS]
    val batteryOptPermission = permissions[PermissionEnum.BATTERY_OPTIMIZATION]

    val isInPermissionState = notificationPermission != PermissionStatus.GRANTED ||
            batteryOptPermission != PermissionStatus.GRANTED

    val activity = LocalActivity.current

    ListeningAppScreenLayout(
        apps = apps,
        isListening = isListening,
        onCheckedChange = dashBoardViewModel::onAppCheckChange,
        onListeningCheckChange = dashBoardViewModel::onListeningStatusChange,
        isInPermissionState = isInPermissionState,
        permissionStatus = permissions,
        onGrantPermissionClick = { permission ->
            if (activity != null) {
                val permissionsRequestedOnce =
                    dashBoardViewModel.permissionsRequestedAteastOnce.value
                permission.requestPermission(activity, permissionsRequestedOnce) {
                    dashBoardViewModel.markPermissionAsRequested(permission)
                }
            }
        },
        onClickNext = onClickNext
    )
}

@Composable
fun ListeningAppScreenLayout(
    apps: List<AppInfo>,
    isListening: Boolean,
    onCheckedChange: (Boolean, AppInfo) -> Unit,
    onListeningCheckChange: (Boolean) -> Unit,
    isInPermissionState: Boolean,
    permissionStatus: Map<PermissionEnum, PermissionStatus>,
    onGrantPermissionClick: (PermissionEnum) -> Unit,
    onClickNext: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            item {
                Spacer(Modifier.height(80.dp))
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
                    stringResource(R.string.listening_screen_rationale),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
                Spacer(Modifier.height(32.dp))
            }

            item {
                EnableListenSubmission(
                    apps = apps,
                    isListening = isListening,
                    onCheckedChange = onCheckedChange,
                    onListeningCheckChange = onListeningCheckChange,
                    isInPermissionState = isInPermissionState
                )
            }

            if (isInPermissionState) {
                item {
                    Spacer(Modifier.height(32.dp))

                    if (permissionStatus[PermissionEnum.READ_NOTIFICATIONS] != PermissionStatus.GRANTED) {
                        PermissionCard(
                            permissionEnum = PermissionEnum.READ_NOTIFICATIONS,
                            isPermanentlyDecline = true,
                            onClick = {
                                onGrantPermissionClick(PermissionEnum.READ_NOTIFICATIONS)
                            },
                            isDisabled = !isListening,
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(32.dp))

                    if (permissionStatus[PermissionEnum.BATTERY_OPTIMIZATION] != PermissionStatus.GRANTED) {
                        PermissionCard(
                            permissionEnum = PermissionEnum.BATTERY_OPTIMIZATION,
                            isPermanentlyDecline = false,
                            onClick = {
                                onGrantPermissionClick(PermissionEnum.BATTERY_OPTIMIZATION)
                            },
                            isDisabled = !isListening,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(36.dp))
                OnboardingYellowButton(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    text = "Done",
                    isEnabled = !(isInPermissionState && isListening),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onClickNext()

                    },
                    onClickWhileDisabled = {
                        Toast.makeText(
                            context,
                            "Please grant permissions or disable listen submission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}


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
            Image(
                modifier = Modifier.size(40.dp),
                bitmap = appInfo.icon.asImageBitmap(),
                contentDescription = "App icon"
            )

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
            onCheckedChange = if (enabled) onCheckedChange else {
                {}
            }
        )
    }
}


@Composable
fun EnableListenSubmission(
    apps: List<AppInfo>,
    isListening: Boolean,
    onCheckedChange: (Boolean, AppInfo) -> Unit,
    onListeningCheckChange: (Boolean) -> Unit,
    isInPermissionState: Boolean,
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
                .animateContentSize(animationSpec = tween(300))
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
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )

                Switch(
                    checked = isListening,
                    onCheckedChange = onListeningCheckChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = isInPermissionState,
                transitionSpec = {
                    createSlideTransition()
                }
            ) { isPermissionRequired ->
                Text(
                    text = if (isPermissionRequired)
                        stringResource(R.string.listen_submission_rationale)
                    else
                        stringResource(R.string.listen_permission_rationale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    lineHeight = 20.sp,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isInPermissionState) {
                apps.forEachIndexed { ind, app ->
                    AppCard(
                        appInfo = app,
                        onCheckedChange = {
                            onCheckedChange(it, app)
                        },
                        enabled = isListening
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (apps.lastIndex != ind) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ListeningAppLayoutPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.ListeningAppScreen))
        ListeningAppScreenLayout(
            onClickNext = {},
            apps = emptyList(),
            isListening = true,
            onCheckedChange = { _, _ -> },
            onListeningCheckChange = {},
            isInPermissionState = true,
            permissionStatus = emptyMap(),
            onGrantPermissionClick = {}
        )
    }
}
