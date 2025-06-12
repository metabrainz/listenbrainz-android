package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.components.OnboardingBlobs
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.onboardingGradient
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@Composable
fun PermissionScreen(dashBoardViewModel: DashBoardViewModel = hiltViewModel(), onExit: () -> Unit) {
    val activity = LocalActivity.current
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val permissionsRequestedOnce by dashBoardViewModel.permissionsRequestedAteastOnce.collectAsState()

    LaunchedEffect(permissions) {
        if (permissions.isEmpty() || permissions.all { it.value == PermissionStatus.GRANTED }) {
            onExit() // Exit if all permissions are granted
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { perm ->
        }
    PermissionScreenBase(
        permissions,
        onGrantPermissionClick = { permission ->
            if (activity != null) {
                permission.requestPermission(activity, permissionsRequestedOnce, {
                    dashBoardViewModel.markPermissionAsRequested(permission)
                    launcher.launch(permission.permission)
                })
            }
        },
        onRejectPermissionClick = {
            onExit() // Continue without accepting permissions
        })
}

@Composable
private fun PermissionScreenBase(
    permissions: Map<PermissionEnum, PermissionStatus>,
    onGrantPermissionClick: (PermissionEnum) -> Unit,
    onRejectPermissionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = onboardingGradient)
    ) {
        val permissionList = permissions.toList()
            .filter { it.second == PermissionStatus.NOT_REQUESTED || it.second == PermissionStatus.DENIED_TWICE }
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
                .padding(vertical = 16.dp, horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            item {
                Spacer(Modifier.height(48.dp))
            }
            item {
                Text(
                    "${permissionList.size} permissions missing",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "ListenBrainz needs permissions for playback, history tracking, and submissions. We respect your privacy and don’t collect or share personal data.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(32.dp))
            }
            items(permissionList.size) { index ->
                PermissionCard(
                    permissionEnum = permissionList[index].first,
                    isPermanentlyDecline = permissionList[index].second == PermissionStatus.DENIED_TWICE,
                ) {
                    onGrantPermissionClick(permissionList[index].first)
                }
                if(index < permissionList.size - 1) {
                    Spacer(Modifier.height(32.dp))
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onRejectPermissionClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                ) {
                    Text(
                        "Continue without accepting permissions.",
                        color = lb_orange,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}


@Composable
private fun PermissionCard(
    permissionEnum: PermissionEnum,
    isPermanentlyDecline: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .background(
                ListenBrainzTheme.colorScheme.background.copy(alpha = 0.75f),
                shape = ListenBrainzTheme.shapes.listenCardSmall
            )
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(permissionEnum.image),
                        contentDescription = "Permission Icon",
                        modifier = Modifier.padding(end = 8.dp),
                        tint = ListenBrainzTheme.colorScheme.text,
                    )
                    Text(
                        text = permissionEnum.title,
                        color = ListenBrainzTheme.colorScheme.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (isPermanentlyDecline)
                        permissionEnum.permanentlyDeclinedRationale else permissionEnum.rationaleText,
                    fontWeight = FontWeight.Normal,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.8f),

                    )
            }
            Spacer(Modifier.height(16.dp))
            OnboardingYellowButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(0.9f),
                text = if (isPermanentlyDecline) "Go to Settings" else "Grant Permission",
                fontSize = 16
            )
        }
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionScreenPreview() {
    ListenBrainzTheme {
        PermissionScreenBase(
            permissions = mapOf(
                PermissionEnum.READ_NOTIFICATIONS to PermissionStatus.DENIED_TWICE,
                PermissionEnum.BATTERY_OPTIMIZATION to PermissionStatus.NOT_REQUESTED
            ),
            onGrantPermissionClick = {},
            onRejectPermissionClick = {}
        )
    }
}