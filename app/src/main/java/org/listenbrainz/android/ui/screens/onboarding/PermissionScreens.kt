package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@Composable
fun PermissionScreen(dashBoardViewModel: DashBoardViewModel = hiltViewModel(), onExit : () -> Unit) {
    val activity = LocalActivity.current
    val permissions by dashBoardViewModel.permissionStatusFlow.collectAsState()
    val permissionsRequestedOnce by dashBoardViewModel.permissionsRequestedAteastOnce.collectAsState()

    LaunchedEffect(permissions) {
        if(permissions.isEmpty() || permissions.all { it.value == PermissionStatus.GRANTED }) {
            onExit() // Exit if all permissions are granted
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {perm->
    }
    PermissionScreenBase(permissions,
        onGrantPermissionClick = {permission->
                if(activity != null){
                    permission.requestPermission(activity, permissionsRequestedOnce ,{
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
private fun PermissionScreenBase(permissions: Map<PermissionEnum, PermissionStatus>,
                                 onGrantPermissionClick: (PermissionEnum)-> Unit,
                                 onRejectPermissionClick: ()-> Unit){
    val permissionList = permissions.toList().filter { it.second == PermissionStatus.NOT_REQUESTED || it.second == PermissionStatus.DENIED_TWICE }
    LazyColumn(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        item{
            Text("${permissionList.size} permissions missing!!")
        }
        items(permissionList.size){index->
            PermissionCard(
                permissionEnum = permissionList[index].first,
                isPermanentlyDecline = permissionList[index].second == PermissionStatus.DENIED_TWICE,
            ) {
                onGrantPermissionClick(permissionList[index].first)
            }
        }
        item{
            TextButton(onClick = onRejectPermissionClick) {
                Text("Continue without accepting permissions.",
                    color = lb_orange)
            }
        }
    }
}


@Composable
private fun PermissionCard(permissionEnum: PermissionEnum, isPermanentlyDecline: Boolean, onClick: () -> Unit){
    Card(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = permissionEnum.title)
        Text(text = if(isPermanentlyDecline) permissionEnum.permanentlyDeclinedRationale else permissionEnum.rationaleText)
        Text(text = permissionEnum.title)
        Button(onClick) {
            Text("Give permission", color = if(isPermanentlyDecline) lb_purple else lb_orange)
        }
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionScreenPreview(){
    ListenBrainzTheme{
        PermissionScreenBase(
            permissions = mapOf(PermissionEnum.READ_NOTIFICATIONS to PermissionStatus.DENIED_TWICE,
                                 PermissionEnum.BATTERY_OPTIMIZATION to PermissionStatus.NOT_REQUESTED),
            onGrantPermissionClick = {},
            onRejectPermissionClick = {}
        )
    }
}