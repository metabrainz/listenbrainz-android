package org.listenbrainz.android.ui.screens.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.listenbrainz.android.R
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.AppPreferencesImpl
import org.listenbrainz.android.util.LBSharedPreferences.STATUS_LOGGED_IN
import org.listenbrainz.android.util.LBSharedPreferences.STATUS_LOGGED_OUT
import org.listenbrainz.android.viewmodel.LoginViewModel

@Composable
fun ProfileScreen(
    context : Context = LocalContext.current,
    viewModel: LoginViewModel = hiltViewModel(),
    appPreferences: AppPreferences = AppPreferencesImpl(context)
) {
    // Initializing app preferences.
    viewModel.appPreferences = appPreferences
    
    var loginStatus by remember { mutableStateOf(viewModel.appPreferences.loginStatus) }
    
    LaunchedEffect(key1 = Unit){
        loginStatus = viewModel.appPreferences.loginStatus
    }
    
    
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Box {
            val comp by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.login))
            LottieAnimation(
                composition = comp,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.requiredHeightIn(max = 400.dp),
            )
        }
        
        Button(
            onClick = {
                when (loginStatus) {
                    STATUS_LOGGED_IN -> {
                        viewModel.logoutUser(context)
                        loginStatus = STATUS_LOGGED_OUT
                    }
                    else -> {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.putExtra("startLogin", true)
                        context.startActivity(intent)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 10.dp, end = 10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface)
        ) {
            Text(
                text = when (loginStatus) {
                    STATUS_LOGGED_IN -> stringResource(id = R.string.logout)
                    else -> stringResource(id = R.string.login)
                },
                color = Color.White,
                fontSize = 16.sp
            )
        }
        
        Text(
            text = when (loginStatus) {
                STATUS_LOGGED_IN -> stringResource(id = R.string.logout_prompt)
                else -> stringResource(id = R.string.login_prompt)
                
            },
            color = MaterialTheme.colors.surface,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp, start = 5.dp, end = 5.dp)
        )
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}