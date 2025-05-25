package org.listenbrainz.android.ui.screens.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_orange

@Composable
fun PermissionScreen(){
    
}

@Composable
private fun PermissionScreenBase(permissions: List<Pair<PermissionEnum,Boolean>>,
                                 onGrantPermissionClick: (PermissionEnum)-> Unit,
                                 onRejectPermissionClick: ()-> Unit){
    LazyColumn(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        item{
            Text("${permissions.size} permissions missing!!")
        }
        items(permissions.size){index->
            PermissionCard(
                permissionEnum = permissions[index].first,
                isPermanentlyDecline = permissions[index].second,
            ) {
                onGrantPermissionClick(permissions[index].first)
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
            Text("Give permission", color = lb_orange)
        }
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PermissionScreenPreview(){
    ListenBrainzTheme{
        PermissionScreenBase(
            permissions = listOf(PermissionEnum.READ_NOTIFICATIONS to true,
                                 PermissionEnum.BATTERY_OPTIMIZATION to false),
            onGrantPermissionClick = {},
            onRejectPermissionClick = {}
        )
    }
}