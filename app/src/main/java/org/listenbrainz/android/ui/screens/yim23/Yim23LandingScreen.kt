package org.listenbrainz.android.ui.screens.yim23

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.listenbrainz.android.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.Yim23ThemeData
import org.listenbrainz.android.ui.components.Yim23ShareButton
import org.listenbrainz.android.ui.theme.Yim23Theme
import org.listenbrainz.android.ui.theme.yim23Blue
import org.listenbrainz.android.ui.theme.yim23Green
import org.listenbrainz.android.ui.theme.yim23Grey
import org.listenbrainz.android.ui.theme.yim23Red
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Yim23HomeScreen(
    viewModel: Yim23ViewModel,
    networkConnectivityViewModel: NetworkConnectivityViewModel,
    navController: NavHostController,
    activity: ComponentActivity,
    context: Context = LocalContext.current
){

    val username by viewModel.getUsernameFlow().collectAsState(initial = "")
    var swipeState by remember {
        mutableStateOf(0)
    }

    Yim23Theme (viewModel.themeType.value) {
        val networkStatus = networkConnectivityViewModel.getNetworkStatusFlow()
            .collectAsState(initial = ConnectivityObserver.NetworkStatus.UNAVAILABLE)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val (x, y) = dragAmount
                        if (y < 0) {
                            if(swipeState == 0){
                                navController.navigate(route = Yim23Screens.YimChartTitleScreen.name)
                                swipeState = 1
                            }
                        }
                    }
                }

        ){
        Column (Modifier.fillMaxSize() , horizontalAlignment = Alignment.CenterHorizontally) {
            when (networkStatus.value) {

                ConnectivityObserver.NetworkStatus.AVAILABLE -> {
                    // Data status checking
                    when (viewModel.yimData.value.status) {
                        Resource.Status.LOADING -> {
                            Toast.makeText(context, "Loading", Toast.LENGTH_SHORT)
                                .show()
                        }

                        Resource.Status.FAILED -> {
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                            activity.finish()
                        }

                        else -> {
                            if (viewModel.yimData.value.data?.payload?.data != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 22.dp)
                                        .fillMaxWidth()
                                        , horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter =
                                        painterResource(id = when (viewModel.themeType.value) {
                                            Yim23ThemeData.greenTheme -> R.drawable.yim23_pick_color_green
                                            Yim23ThemeData.redTheme -> R.drawable.yim23_pick_color_red
                                            Yim23ThemeData.blueTheme -> R.drawable.yim23_pick_color_blue
                                            Yim23ThemeData.grayTheme -> R.drawable.yim23_pick_color_grey
                                            else -> R.drawable.yim23_pick_color_green
                                        }),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(31.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ColorPicker(color = yim23Green, onClick = {
                                        viewModel.themeType.value = Yim23ThemeData.greenTheme
                                    })
                                    ColorPicker(color = yim23Red, onClick = {
                                        viewModel.themeType.value = Yim23ThemeData.redTheme
                                    })
                                    ColorPicker(color = yim23Blue, onClick = {
                                        viewModel.themeType.value = Yim23ThemeData.blueTheme
                                    })
                                    ColorPicker(color = yim23Grey, onClick = {
                                        viewModel.themeType.value = Yim23ThemeData.grayTheme
                                    })
                                }
                                Text(
                                    text = "#YEAR IN MUSIC",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.paddingFromBaseline(top = 53.dp)
                                )
                                Image(painter = painterResource(
                                    id = when (viewModel.themeType.value) {
                                        Yim23ThemeData.greenTheme -> R.drawable.yim23_2023_green
                                        Yim23ThemeData.redTheme -> R.drawable.yim23_2023_red
                                        Yim23ThemeData.blueTheme -> R.drawable.yim23_2023_blue
                                        Yim23ThemeData.grayTheme -> R.drawable.yim23_2023_grey
                                    },
                                ), modifier = Modifier
                                    .paddingFromBaseline(top = 80.dp)
                                    .width(277.dp)
                                    .height(135.dp), contentDescription = "")
                                IconButton(onClick =
                                {navController.navigate(route = Yim23Screens.YimChartTitleScreen.name)} ,
                                    modifier = Modifier.paddingFromBaseline(top = 75.dp)) {
                                    Icon(imageVector =
                                    ImageVector.vectorResource(R.drawable.yim23_down_arrow_green) ,
                                        contentDescription = "Yim23 down icon" ,
                                        tint = MaterialTheme.colorScheme.onBackground)
                                }
                                Box(modifier = Modifier
                                    .background(MaterialTheme.colorScheme.onBackground)){
                                    Column (verticalArrangement = Arrangement.SpaceEvenly ,
                                        horizontalAlignment = Alignment.CenterHorizontally ,
                                        modifier = Modifier.fillMaxWidth()) {
                                        Text(username.uppercase(),style=MaterialTheme.typography.titleLarge ,
                                            color = MaterialTheme.colorScheme.background ,
                                            modifier = Modifier.paddingFromBaseline(top = 60.dp))
                                        Row (modifier = Modifier.fillMaxWidth().paddingFromBaseline(bottom = 60.dp) ,
                                            verticalAlignment = Alignment.CenterVertically ,
                                            horizontalArrangement = Arrangement.Center) {
                                            Yim23ShareButton()
                                            ListenBrainzProfileButton()
                                            AddUser()
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 70.dp, top = 22.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = when (viewModel.themeType.value) {
                                            Yim23ThemeData.greenTheme -> R.drawable.yim23_pick_color_green
                                            Yim23ThemeData.redTheme -> R.drawable.yim23_pick_color_red
                                            Yim23ThemeData.blueTheme -> R.drawable.yim23_pick_color_blue
                                            Yim23ThemeData.grayTheme -> R.drawable.yim23_pick_color_grey
                                            else -> R.drawable.yim23_pick_color_green
                                        }),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .width(160.dp)
                                            .height(31.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ColorPicker(color = yim23Green, onClick = {

                                        viewModel.themeType = mutableStateOf(Yim23ThemeData.greenTheme)
                                    })
                                    ColorPicker(color = yim23Red, onClick = {

                                        viewModel.themeType = mutableStateOf(Yim23ThemeData.redTheme)
                                    })
                                    ColorPicker(color = yim23Blue, onClick = {

                                        viewModel.themeType = mutableStateOf(Yim23ThemeData.blueTheme)
                                    })
                                    ColorPicker(color = yim23Grey, onClick = {

                                        viewModel.themeType = mutableStateOf(Yim23ThemeData.grayTheme)
                                    })
                                }
                                Text(
                                    text = "#YEAR IN MUSIC",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.paddingFromBaseline(top = 53.dp)
                                )
                                Image(painter = painterResource(id = R.drawable.yim23_flower_green) ,
                                    contentDescription = "" , modifier = Modifier
                                    .paddingFromBaseline(60.dp)
                                    .height(48.dp)
                                    .width(103.dp))
                                Text("Oh no! We don't have enough 2023 statistics for ${username}." ,
                                    style = MaterialTheme.typography.bodyMedium ,
                                    color = MaterialTheme.colorScheme.onBackground ,
                                    textAlign = TextAlign.Center ,
                                    modifier = Modifier.paddingFromBaseline(top = 60.dp))
                                Text("Submit enough listens before the end of December to " +
                                        "generate your #yearinmusic next year" ,
                                    color = MaterialTheme.colorScheme.onBackground ,
                                    style = MaterialTheme.typography.bodyMedium ,
                                    modifier = Modifier.paddingFromBaseline(top = 40.dp , bottom=40.dp) ,
                                    textAlign = TextAlign.Center)
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.onBackground)){
                                    Column (verticalArrangement = Arrangement.SpaceEvenly ,
                                        horizontalAlignment = Alignment.CenterHorizontally , ) {
                                        Text(username.uppercase(),
                                            style=MaterialTheme.typography.titleLarge ,
                                            color = MaterialTheme.colorScheme.background ,
                                            modifier = Modifier.paddingFromBaseline(top = 40.dp))
                                        Row (modifier = Modifier.fillMaxWidth() ,
                                            verticalAlignment = Alignment.CenterVertically ,
                                            horizontalArrangement = Arrangement.Center) {
                                            Yim23ShareButton()
                                            ListenBrainzProfileButton()
                                            AddUser()
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                else -> {Toast.makeText(context , "Please check your internet connection" ,
                    Toast.LENGTH_LONG)}
                }
            }
        }
    }
}

@Composable
fun ColorPicker(color: Color , onClick : () -> Unit ) {
    Button(onClick = { onClick() } , colors = ButtonDefaults.buttonColors(backgroundColor = color),modifier = Modifier
        .padding(11.dp)
        .width(32.dp)
        .height(32.dp),
     shape =  RoundedCornerShape(100) 
    ) {}
}

@Composable
fun ListenBrainzProfileButton() {
    Button(onClick = { /*TODO*/ } , colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface) , modifier = Modifier
        .padding(11.dp)
        .height(49.dp)) {
        Text("ListenBrainz Profile" , style = MaterialTheme.typography.titleMedium , color = MaterialTheme.colorScheme.background)
    }
}

@Composable
fun AddUser() {
    Button(onClick = { /*TODO*/ } , colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface)) {
        Icon(imageVector = ImageVector.vectorResource(R.drawable.yim23_add_user) , contentDescription = "Yim23 share icon" , tint = MaterialTheme.colorScheme.background)
    }
}