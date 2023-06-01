package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AppNavigationItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(
    shouldScrollToTop: MutableState<Boolean>,
    navController: NavController = rememberNavController(),
    backdropScaffoldState: BackdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
) {
    val items = listOf(
        AppNavigationItem.Home,
        AppNavigationItem.BrainzPlayer,
//        AppNavigationItem.Explore,
        AppNavigationItem.Profile,
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        elevation = 0.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        items.forEach { item ->
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            BottomNavigationItem(
                icon = {
                    Icon(
                        painterResource(id = selected
                            .takeIf { it }
                            ?.let { item.iconSelected }
                            ?: item.iconUnselected),
                        modifier = Modifier
                        .size(24.dp)
                        .padding(top = 5.dp), contentDescription = item.title, tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = colorResource(id = R.color.gray),
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    coroutineScope.launch {
                        shouldScrollToTop.value = true

                        // A quick way to navigate to back layer content.
                        backdropScaffoldState.reveal()
                        
                        navController.navigate(item.route){
                            // Avoid building large backstack
                            popUpTo(AppNavigationItem.Home.route){
                                saveState = true
                            }
                            // Avoid copies
                            launchSingleTop = true
                            // Restore previous state
                            restoreState = true
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
    BottomNavigationBar(shouldScrollToTop = remember { mutableStateOf(false) }, navController = rememberNavController())
}