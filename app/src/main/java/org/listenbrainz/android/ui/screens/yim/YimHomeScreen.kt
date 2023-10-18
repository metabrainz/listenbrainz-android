package org.listenbrainz.android.ui.screens.yim

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.listenbrainz.android.R
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.model.yimdata.YimShareable
import org.listenbrainz.android.ui.components.YimShareButton
import org.listenbrainz.android.ui.theme.LocalYimPaddings
import org.listenbrainz.android.ui.theme.YearInMusicTheme
import org.listenbrainz.android.ui.theme.YimPaddings
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YimHomeScreen(
    viewModel: YimViewModel,
    networkConnectivityViewModel: NetworkConnectivityViewModel,
    navController: NavHostController,
    activity: ComponentActivity,
    paddings: YimPaddings = LocalYimPaddings.current,
    context: Context = LocalContext.current
){
    YearInMusicTheme(redTheme = true) {
        
        var startAnimations by remember { mutableStateOf(false) }
        val swipeableState = rememberSwipeableState(initialValue = false)
        var isYimAvailable by remember { mutableStateOf(false) }
        val networkStatus = networkConnectivityViewModel.getNetworkStatusFlow()
            .collectAsState(initial = ConnectivityObserver.NetworkStatus.UNAVAILABLE)
        
        LaunchedEffect(key1 = true){
            startAnimations = true
        }
        
        // What happens when user swipes up
        LaunchedEffect(key1 = swipeableState.currentValue){
            if (swipeableState.currentValue) {
                when (networkStatus.value) {
                    ConnectivityObserver.NetworkStatus.AVAILABLE -> {
                        // Data status checking
                        when (viewModel.yimData.value.status){
                            Resource.Status.LOADING -> {
                                Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
                            }
                            Resource.Status.FAILED -> {
                                Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                                activity.finish()
                            }
                            else -> {
                                // Checks if user has less listens, i.e., No yim data available.
                                if (viewModel.yimData.value.data?.payload?.data != null) {
                                    navController.navigate(route = YimScreens.YimTopAlbumsScreen.name)
                                }else{
                                    Toast.makeText(context, "Seems like you have very less listens :(", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(context, "Try again next year!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Please check your internet connection!",
                            Toast.LENGTH_LONG
                        ).show()
                        activity.finish()
                    }
                }
            }
            swipeableState.animateTo(false, anim = tween(delayMillis = 0))
        }
    
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            /** Greeting **/
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = paddings.largePadding)
                    .swipeable(
                        state = swipeableState,
                        orientation = Orientation.Vertical,
                        anchors = mapOf(
                            0f to false,
                            -600f to true
                        ),
                        /*thresholds = { _: Boolean, _: Boolean ->
                            FractionalThreshold(0.9f)
                        }*/
                    )
                    .graphicsLayer {
                        translationY = swipeableState.offset.value
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Yim logo
                Image(
                    modifier = Modifier.testTag(stringResource(id = R.string.tt_yim_home_logo)),
                    painter = painterResource(id = R.drawable.yim_logo),
                    contentDescription = "Year in Music logo"
                )
            
                Spacer(modifier = Modifier.height(paddings.smallPadding))
            
                // Yim Text
                Text(
                    text = "YOUR\n#YEARINMUSIC",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.background,
                    textAlign = TextAlign.Center,
                )
            
                Spacer(modifier = Modifier.height(paddings.smallPadding))

                // Loading state
                Column(modifier = Modifier
                    .height(30.dp)
                    .width(15.dp)) {
                    
                    when (viewModel.yimData.value.status) {
                        Resource.Status.LOADING -> {
                            isYimAvailable = false
                            CircularProgressIndicator(
                                modifier = Modifier.size(15.dp),
                                color = MaterialTheme.colorScheme.background,
                                strokeWidth = 3.dp
                            )
                        }
                        Resource.Status.SUCCESS -> {
    
                            // Down Arrow animation
                            val infiniteAnim = rememberInfiniteTransition(label = "infiniteAnim")
                            val animValue by infiniteAnim.animateFloat(
                                initialValue = 0f,
                                targetValue = 45f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 600, delayMillis = 200),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "animValue"
                            )
                            
                            if (viewModel.yimData.value.data?.payload?.data != null) {
                                Icon(
                                    painter = painterResource(id = R.drawable.yim_arrow_down),
                                    contentDescription = "Swipe down to continue.",
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .graphicsLayer {
                                            translationY = animValue
                                        }
                                )
                                isYimAvailable = true
                            }else {
                                // User is new with less listens
                                isYimAvailable = false
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.background,
                                    contentDescription = "Some error occurred"
                                )
                            }
                        }
                        else -> {
                            // Any error occurs
                            isYimAvailable = false
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.background,
                                contentDescription = "Some error occurred"
                            )
                        }
                    }
                }
                
            
            }
        
            /** Bottom Window **/
            // Bottom Window height animation
            val bottomBarHeight by animateDpAsState(
                targetValue = if (startAnimations) 180.dp else 0.dp,
                animationSpec = tween(durationMillis = 1000),
                label = "bottomBarHeight"
            )
        
            // Bottom window content
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(bottomBarHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                val username by viewModel.getUsernameFlow().collectAsState(initial = "")
                
                // Bottom Window text
                Text(
                    modifier = Modifier.padding(paddings.defaultPadding),
                    maxLines = 2,           // If username is very long
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.roboto_light))
                            )
                        ){
                            append("Share ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ){
                            append("$username's")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.roboto_light))
                            )
                        ){
                            append(" year")
                        }
                    }
                )
            
                Spacer(modifier = Modifier.height(paddings.largePadding))

                // Share Icon
                YimShareButton(
                    typeOfImage = arrayOf(YimShareable.TRACKS),
                    viewModel = viewModel,
                    disableButton = !isYimAvailable     // Make share function unavailable if user has no yim.
                )
            }
        }
    }
}



