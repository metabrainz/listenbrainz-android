package org.listenbrainz.android.ui.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.ui.screens.login.LoginActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavController = rememberNavController(),
    activity: Activity,
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
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
        val coroutineScope = rememberCoroutineScope()
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 5.dp), contentDescription = item.title, tint = Color.Unspecified) },
                label = { Text(text = item.title, fontSize = 10.sp) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = true,
                onClick = {
                    coroutineScope.launch {
                        // A quick way to navigate to back layer content.
                        backdropScaffoldState.reveal()
                        
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
                    
                }
            
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = rememberNavController(), activity = DashboardActivity())
}