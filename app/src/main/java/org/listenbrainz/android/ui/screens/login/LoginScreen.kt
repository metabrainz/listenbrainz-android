package org.listenbrainz.android.ui.screens.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AccessToken
import org.listenbrainz.android.model.UserInfo
import org.listenbrainz.android.ui.screens.dashboard.DashboardActivity
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.util.ListenBrainzServiceGenerator
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.viewmodel.LoginViewModel

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val loginViewModel = hiltViewModel<LoginViewModel>()

    val accessToken: AccessToken? = loginViewModel.accessTokenLiveData!!.observeAsState().value
    if (accessToken != null && LBSharedPreferences.loginStatus == LBSharedPreferences.STATUS_LOGGED_OUT) {
        saveOAuthToken(accessToken, loginViewModel, context)
    }

    val userInfo: UserInfo? = loginViewModel.userInfoLiveData!!.observeAsState().value
    if (userInfo != null && LBSharedPreferences.loginStatus == LBSharedPreferences.STATUS_LOGGED_OUT) {
        saveUserInfo(userInfo, context)
    }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box() {
            val comp by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.login))
            LottieAnimation(
                composition = comp,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.requiredHeightIn(max = 400.dp),
            )
        }

        Button(
            onClick = {
                when (LBSharedPreferences.loginStatus) {
                    LBSharedPreferences.STATUS_LOGGED_IN -> logoutUser(context)
                    else -> startLogin(context)

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 10.dp, end = 10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface)
        ) {
            Text(text = when (LBSharedPreferences.loginStatus) {
                LBSharedPreferences.STATUS_LOGGED_IN -> stringResource(id = R.string.logout)
                else -> stringResource(id = R.string.login)

            }, color = Color.White)
        }

        Text(
            text = when (LBSharedPreferences.loginStatus) {
                LBSharedPreferences.STATUS_LOGGED_IN -> stringResource(id = R.string.logout_prompt)
                else -> stringResource(id = R.string.login_prompt)

            },
            color = MaterialTheme.colors.surface,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 50.dp, start = 5.dp, end = 5.dp)
        )
    }

    OnResumeListener{
        when(it){
            Lifecycle.Event.ON_RESUME ->{
                val activity = context.findActivity()
                val intent = activity?.intent
                val callbackUri = intent?.data
                if (callbackUri != null && callbackUri.toString().startsWith(ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI)) {
                    val code = callbackUri.getQueryParameter("code")
                    if (code != null) {
                        loginViewModel.fetchAccessToken(code)
                    }
                }
            }
            else -> {}
        }
    }
}


private fun startLogin(context: Context) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(
            ListenBrainzServiceGenerator.AUTH_BASE_URL
                    + "authorize"
                    + "?response_type=code"
                    + "&client_id=" + ListenBrainzServiceGenerator.CLIENT_ID
                    + "&redirect_uri=" + ListenBrainzServiceGenerator.OAUTH_REDIRECT_URI
                    + "&scope=profile%20collection%20tag%20rating"
        )
    )
    context.startActivity(intent)
}


private fun saveOAuthToken(
    accessToken: AccessToken?,
    loginViewModel: LoginViewModel,
    context: Context
) {
    when {
        accessToken != null -> {
            Log.d(accessToken.accessToken)
            LBSharedPreferences.saveOAuthToken(accessToken)
            loginViewModel.fetchUserInfo()
        }
        else -> {
            Toast.makeText(
                context,
                "Failed to obtain access token ",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}


private fun saveUserInfo(userInfo: UserInfo?, context: Context) {
    if (userInfo != null &&
        LBSharedPreferences.loginStatus == LBSharedPreferences.STATUS_LOGGED_OUT
    ) {
        LBSharedPreferences.saveUserInfo(userInfo)
        Toast.makeText(
            context,
            "Login successful. " + userInfo.username + " is now logged in.",
            Toast.LENGTH_LONG
        ).show()
        context.startActivity(Intent(context, DashboardActivity::class.java))
    }
}

private fun logoutUser(context: Context) {
    LBSharedPreferences.logoutUser()
    Toast.makeText(
        context,
        "User has successfully logged out.",
        Toast.LENGTH_LONG
    ).show()
    context.startActivity(Intent(context, DashboardActivity::class.java))
}

@Composable
fun OnResumeListener(OnEvent : (event : Lifecycle.Event)->Unit){
    val eventHandler = rememberUpdatedState(newValue = OnEvent)
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value){
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver {
                _, event ->  eventHandler.value(event)
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

