package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.screens.components.YimShareButton
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun YimHomeScreen(
    viewModel: YimViewModel,
    navController: NavHostController,
    activity: YearInMusicActivity,
    paddings: YimPaddings = LocalYimPaddings.current,
    context: Context = LocalContext.current
){
    YearInMusicTheme(redTheme = true) {
        
        var startAnimations by remember { mutableStateOf(false) }
        val swipeableState = rememberSwipeableState(initialValue = false)
        
        LaunchedEffect(key1 = true){
            startAnimations = true
        }
        
        // What happens when user swipes up
        LaunchedEffect(key1 = swipeableState.isAnimationRunning){
            if (swipeableState.isAnimationRunning) {
                when (viewModel.getNetworkStatus()) {
                    ConnectivityObserver.NetworkStatus.Available -> {
                        // Data status checking
                        when (viewModel.yimData.value.status){
                            Resource.Status.LOADING -> {
                                Toast.makeText(context, "Loading...", Toast.LENGTH_LONG).show()
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
                                    Toast.makeText(context, "Seems like you have very less listens :(", Toast.LENGTH_LONG).show()
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
            swipeableState.animateTo(false, anim = tween(delayMillis = 1000))
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
                    .offset(y = swipeableState.offset.value.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Yim logo
                Image(
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
            
                // Down Arrow animation
                val infiniteAnim = rememberInfiniteTransition()
                val animValue by infiniteAnim.animateFloat(
                    initialValue = 0f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, delayMillis = 200),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                // Down Arrow
                Icon(
                    painter = painterResource(id = R.drawable.yim_arrow_down),
                    contentDescription = "Swipe down to continue.",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(15.dp)
                        .offset(y = animValue.dp)
                )
            
            }
        
            /** Bottom Window **/
            // Bottom Window height animation
            val bottomBarHeight by animateDpAsState(
                targetValue = if (startAnimations) 180.dp else 0.dp,
                animationSpec = tween(durationMillis = 1000)
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
                            append(viewModel.getUserName() + "'s")
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
                YimShareButton(isRedTheme = true)
            }
        }
    }
}



