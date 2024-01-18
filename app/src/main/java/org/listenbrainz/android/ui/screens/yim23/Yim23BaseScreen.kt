package org.listenbrainz.android.ui.screens.yim23

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.ui.components.Yim23Footer
import org.listenbrainz.android.ui.components.Yim23Header
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.viewmodel.Yim23ViewModel

@Composable
fun Yim23BaseScreen(
    viewModel: Yim23ViewModel,
    navController: NavController,
    footerText: String,
    isUsername: Boolean,
    downScreen: Yim23Screens,
    shareable: YimShareable? = null,
    content: @Composable () -> Unit,
){
    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    var swipeState by remember {
        mutableStateOf(0)
    }
    Yim23Theme(themeType = viewModel.themeType.value) {
        Column (modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    val (x, y) = dragAmount
                    if (y < 0) {
                        if (swipeState == 0) {
                            navController.navigate(route = downScreen.name)
                            swipeState = 1
                        }
                    } else {
                        if (swipeState == 0) {
                            navController.popBackStack()
                            swipeState = 1
                        }
                    }
                }
            },
                verticalArrangement = Arrangement.SpaceBetween) {
            Yim23Header(username = username, navController = navController)
            content()
            if(shareable == null){
                Yim23Footer(footerText = footerText, isUsername = isUsername,
                    navController = navController, downScreen = downScreen)
            }
            else{
                Column(modifier = Modifier.fillMaxWidth() , horizontalAlignment = Alignment.CenterHorizontally) {
                    Yim23ShareButton(
                        viewModel = viewModel,
                        typeOfImage = arrayOf(shareable)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    Yim23Footer(footerText = footerText, isUsername = isUsername,
                        navController = navController, downScreen = downScreen)
                }
            }

            
        }
    }
}