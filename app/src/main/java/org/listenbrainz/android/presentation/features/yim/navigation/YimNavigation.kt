package org.listenbrainz.android.presentation.features.yim.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.screens.YimHomeScreen
import org.listenbrainz.android.presentation.features.yim.screens.YimMainScreen

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun YimNavigation(
    viewModel: YimViewModel,
    activity: YearInMusicActivity
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = YimScreens.YimHomeScreen.name
    ){
        // Add all Yim screens here
        
        composable(YimScreens.YimHomeScreen.name){
            YimHomeScreen(viewModel = viewModel, navController = navController, activity = activity)
        }
    
        composable(YimScreens.YimMainScreen.name){
            YimMainScreen(viewModel = viewModel, navController = navController)
        }
        
    }
}