package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import org.listenbrainz.android.model.yimdata.Yim23Screens

@Composable
fun Yim23AutomaticScroll (
    navController: NavController,
    time : Int,
    downScreen : Yim23Screens
){
    var timeLeft by remember {
        mutableStateOf(time)
    }

    LaunchedEffect(key1 = timeLeft) {
        while (timeLeft > 0) {
            delay(1L)
            timeLeft--
        }
    }
    if(timeLeft == 0){
        navController.navigate(route = downScreen.name) // Make sure this happens only once
        timeLeft = -1
    }
}