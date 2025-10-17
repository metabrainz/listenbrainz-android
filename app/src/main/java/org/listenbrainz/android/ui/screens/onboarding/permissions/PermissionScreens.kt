package org.listenbrainz.android.ui.screens.onboarding.permissions

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.R
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.components.FloatingContentAwareLayout
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingSupportButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_yellow
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@Composable
fun PermissionScreen(dashBoardViewModel: DashBoardViewModel = hiltViewModel(),
                     onExitAfterGrantingAllPermissions: ()-> Unit,
                     onExit: () -> Unit) {
    val activity = LocalActivity.current
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val permissionsRequestedOnce by dashBoardViewModel.permissionsRequestedAteastOnce.collectAsState()
    val filteredPermissions = permissions.filter { it.key != PermissionEnum.BATTERY_OPTIMIZATION && it.key != PermissionEnum.READ_NOTIFICATIONS }

    LaunchedEffect(filteredPermissions) {
        if (filteredPermissions.isEmpty() || filteredPermissions.all { it.value == PermissionStatus.GRANTED }) {
            onExitAfterGrantingAllPermissions() // Exit if all permissions are granted
        }
    }


    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { perm ->
        }
    PermissionScreenBase(
        filteredPermissions,
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
    FloatingContentAwareLayout(
        modifier = Modifier
            .fillMaxSize(),
        buttonAlignment = Alignment.BottomEnd,
        floatingContent = {
            ExtendedFloatingActionButton(
                onClick = onRejectPermissionClick,
                containerColor = lb_yellow,
                contentColor = ListenBrainzTheme.colorScheme.text,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Skip",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { buttonSize ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val permissionList = permissions.toList()
                .filter { it.second == PermissionStatus.NOT_REQUESTED || it.second == PermissionStatus.DENIED_TWICE }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                item{
                    Spacer(Modifier
                        .statusBarsPadding()
                        .height(100.dp))
                }

                item {
                    Text(
                        "Required Permissions",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.permission_screen_rationale),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(0.9f)
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
                item{
                    Spacer(Modifier.height(buttonSize.height + 16.dp)) // Add space for the floating button
                }
            }
            OnboardingBackButton(modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 8.dp))
            OnboardingSupportButton(modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(top = 8.dp , end = 8.dp)
            )
        }
    }
}


@Composable
fun PermissionCard(
    permissionEnum: PermissionEnum,
    isPermanentlyDecline: Boolean,
    modifier: Modifier = Modifier,
    isDisabled: Boolean = false,
    onClick: () -> Unit
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
            .alpha(if(isDisabled)0.5f else 1f)
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
                    fontSize = 16.sp
                    )
            }
            Spacer(Modifier.height(16.dp))
            OnboardingYellowButton(
                onClick = onClick,
                icon = if(isPermanentlyDecline) R.drawable.ic_redirect else null,
                modifier = Modifier.fillMaxWidth(0.9f),
                isEnabled = !isDisabled,
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
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.PermissionScreen))
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