package org.listenbrainz.android.presentation.features.yim.screens

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.listenbrainz.android.R
import org.listenbrainz.android.presentation.features.yim.YearInMusicActivity
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.navigation.YimScreens
import org.listenbrainz.android.presentation.features.yim.ui.theme.LocalYimPaddings
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme
import org.listenbrainz.android.presentation.features.yim.ui.theme.YimPaddings
import org.listenbrainz.android.util.ConnectivityObserver
import java.util.Timer
import java.util.TimerTask

@OptIn(ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.N)
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
        
        SideEffect {
            startAnimations = true
        }
        
        // What happens when user swipes up
        LaunchedEffect(key1 = swipeableState.targetValue){
            if (swipeableState.targetValue) {
                when (viewModel.getNetworkStatus()) {
                    ConnectivityObserver.NetworkStatus.Available -> navController.navigate(route = YimScreens.YimMainScreen.name)
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
                    )
                    .offset(y = swipeableState.offset.value.dp),
                    //.alpha(swipeableState.offset.value),
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
            
                // Down Arrow
                val infiniteAnim = rememberInfiniteTransition()
                val animValue by infiniteAnim.animateFloat(
                    initialValue = 0f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, delayMillis = 200),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            
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
                    modifier = Modifier.padding(paddings.DefaultPadding),
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
                IconButton(onClick = { /*TODO: Implement share functionality.*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.yim_share_yellow),
                        contentDescription = "Share your Year in Music",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize()
                    )
                }
            }
        
        
        }
    }
}


