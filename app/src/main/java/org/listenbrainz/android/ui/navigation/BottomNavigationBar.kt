package org.listenbrainz.android.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.screens.login.LoginActivity

@Composable
fun BottomNavigationBar(
    navController: NavController = rememberNavController(),
    activity: Activity
) {
    val items = listOf(
        AppNavigationItem.Home,
        AppNavigationItem.BrainzPlayer,
        AppNavigationItem.Listens,
        AppNavigationItem.Profile,
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        elevation = 0.dp
    ) {
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon),
                    modifier = Modifier.size(28.dp).padding(top = 4.dp), contentDescription = item.title, tint = Color.Unspecified) },
                label = { Text(text = item.title, fontSize = 11.sp) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = true,
                onClick = {
                    // TODO: remove this after profile page has been converted to composable
                    if (item == AppNavigationItem.Profile){
                        activity.startActivity(Intent(activity,LoginActivity::class.java))
                    }else{
                        navController.navigate(item.route){
                            // Avoid building large backstack
                            popUpTo(AppNavigationItem.Home.route){
                                saveState = true
                            }
                            // Avoid copies
                            launchSingleTop = true
                            // Restore previous state
                            restoreState = true
                            // TODO: Implement refresh for listens Screen.
                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController(), activity = DashboardActivity())
}