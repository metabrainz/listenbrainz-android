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
    var alreadyScrolled by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (alreadyScrolled)
            return@LaunchedEffect
        delay(time.toLong())
        navController.navigate(downScreen.name)
        alreadyScrolled = true
    }
}