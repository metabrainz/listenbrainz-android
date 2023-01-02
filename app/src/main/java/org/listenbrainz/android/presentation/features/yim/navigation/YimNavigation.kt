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
import org.listenbrainz.android.presentation.features.yim.screens.YimTopAlbumsScreen

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun YimNavigation(
    yimViewModel: YimViewModel,
    activity: YearInMusicActivity,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = YimScreens.YimHomeScreen.name
    ){
        // Add all Yim screens here
        
        composable(YimScreens.YimHomeScreen.name){
            YimHomeScreen(viewModel = yimViewModel, navController = navController, activity = activity)
        }
    
        composable(YimScreens.YimTopAlbumsScreen.name){
            YimTopAlbumsScreen(yimViewModel = yimViewModel, navController = navController)
        }
        
    }
}