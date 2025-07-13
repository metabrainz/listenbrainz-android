package org.listenbrainz.android.ui.screens.onboarding.auth

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.ListenBrainzWebClient
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.LaunchedEffectMainThread
import org.listenbrainz.android.util.Utils.LaunchedEffectUnit
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.viewmodel.ListensViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val TAG = "ListenBrainzLogin"

// Sealed class to represent all possible login states
sealed class LoginState {
    data object Idle : LoginState()
    data class Loading(val message: String) : LoginState()
    data object SubmittingCredentials : LoginState()
    data object AuthenticatingWithServer : LoginState()
    data object VerifyingToken : LoginState()
    data class Error(val message: String) : LoginState()
    data class Success(val message: String) : LoginState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenBrainzLogin(
    modifier: Modifier = Modifier,
    onLoginFinished: () -> Unit
) {
    val viewModel = hiltViewModel<ListensViewModel>()
    val scope = rememberCoroutineScope()

    var loginState by remember { mutableStateOf<LoginState>(LoginState.Idle) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var loginTimeoutJob by remember { mutableStateOf<Job?>(null) }

    // Login timeout functions
    val startTimeout = {
        loginTimeoutJob?.cancel()
        loginTimeoutJob = scope.launch {
            Log.d(TAG,"Starting login timer")
            repeat(30) { // 30 seconds timeout
                delay(1.seconds)
                if (loginState is LoginState.Success || loginState is LoginState.Error) {
                    return@launch // Exit if already logged in or error occurred
                }
                Log.d(TAG,"Login timer tick: ${it + 1} seconds")
            }
            if (loginState !is LoginState.Success && isLoggingIn) {
                Log.d(TAG,"Login timeout")
                loginState = LoginState.Error("Login timed out. Please try again.")
                isLoggingIn = false
            }
        }
    }

    val clearTimeout = {
        Log.d(TAG,"Login timer cleared")
        loginTimeoutJob?.cancel()
        loginTimeoutJob = null
//        loginState = LoginState.Idle
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Only create ListenBrainzClient when user is actively logging in
        if (isLoggingIn) {
            ListenBrainzClient(
                modifier = Modifier.size(1.dp)
                    .alpha(1f),
                username = username,
                password = password,
                onLoad = { resource ->
                    Log.d(TAG, "Load state: ${loginState}, data: ${resource.data}, error: ${resource.error}")
                    //Not letting screen reload if loginState is Error
                    if(loginState !is LoginState.Error){
                    when {
                        resource.isSuccess -> {
                            // We got the token, now validate it
                            loginState = LoginState.VerifyingToken
                            // Continue with token validation
                            scope.launch {
                                val validationResult =
                                    viewModel.validateAndSaveUserDetails(resource.data!!)
                                loginState =
                                    if (validationResult.status == Resource.Status.SUCCESS) {
                                        clearTimeout()
                                        LoginState.Success("Login successful!")
                                    } else {
                                        clearTimeout()
                                        LoginState.Error(
                                            validationResult.error?.actualResponse
                                                ?: "Token validation failed"
                                        )
                                    }

                                // After success or final failure, transition back to main flow
                                if (loginState is LoginState.Success) {
                                    delay(1500.milliseconds)
                                    onLoginFinished()
                                } else {
                                    isLoggingIn = false
                                }
                            }
                        }

                        resource.isFailed -> {
                            loginState =
                                LoginState.Error(resource.error?.actualResponse ?: "Login failed")
                            isLoggingIn = false
                            clearTimeout()
                        }

                        resource.isLoading -> {
                            loginState = when {
                                loginState == LoginState.SubmittingCredentials -> LoginState.AuthenticatingWithServer
                                loginState !is LoginState.Loading -> LoginState.Loading("Connecting...")
                                else -> loginState
                            }
                        }
                    }
                    }
                },
                onPageLoadStateChange = { isLoading, message ->
                    if (isLoading && loginState !is LoginState.Error) {
                        loginState = LoginState.Loading(message ?: "Loading...")
                    }
                }
            )
        }

        // Main login UI
        LoginScreenLayout(
            username = username,
            password = password,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            error = if (loginState is LoginState.Error) (loginState as LoginState.Error).message else null,
            isLoading = loginState is LoginState.Loading || loginState is LoginState.VerifyingToken ||
                       loginState is LoginState.SubmittingCredentials || loginState is LoginState.AuthenticatingWithServer,
            onLoginClick = {
                // Validate form
                if (username.isBlank() || password.isBlank()) {
                    loginState = LoginState.Error("Username and password cannot be empty")
                    return@LoginScreenLayout
                }

                // Start timeout for login process
                startTimeout()

                // Start the login process
                loginState = LoginState.SubmittingCredentials
                isLoggingIn = true
            }
        )

        if (loginState !is LoginState.Idle) {
            val showDialog = loginState is LoginState.Loading ||
                            loginState is LoginState.VerifyingToken ||
                            loginState is LoginState.Error ||
                            loginState is LoginState.Success ||
                            loginState is LoginState.SubmittingCredentials ||
                            loginState is LoginState.AuthenticatingWithServer

            if (showDialog) {
                AlertDialog(
                    containerColor = ListenBrainzTheme.colorScheme.background,
                    onDismissRequest = {
                        // Only allow dismissing in error state
                        if (loginState is LoginState.Error) {
                            loginState = LoginState.Idle
                            clearTimeout()
                        }
                    },
                    title = {
                        Text(
                            text = when (loginState) {
                                is LoginState.Error -> "Login Error"
                                is LoginState.Success -> "Success"
                                else -> "Signing In"
                            },
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (loginState) {
                                is LoginState.Loading,
                                is LoginState.SubmittingCredentials,
                                is LoginState.AuthenticatingWithServer,
                                is LoginState.VerifyingToken -> {
                                    LoadingAnimation()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = when (loginState) {
                                            is LoginState.Loading -> (loginState as LoginState.Loading).message
                                            is LoginState.SubmittingCredentials -> "Submitting credentials..."
                                            is LoginState.AuthenticatingWithServer -> "Authenticating..."
                                            is LoginState.VerifyingToken -> "Verifying token..."
                                            else -> "Loading..."
                                        },
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                is LoginState.Error -> {
                                    Text(
                                        text = (loginState as LoginState.Error).message,
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                is LoginState.Success -> {
                                    Text(
                                        text = (loginState as LoginState.Success).message,
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                else -> {}
                            }
                        }
                    },
                    confirmButton = {
                        if (loginState is LoginState.Error) {
                            Text(
                                text = "Try Again",
                                color = ListenBrainzTheme.colorScheme.text,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        loginState = LoginState.Idle
                                        clearTimeout()
                                    }
                            )
                        }
                    },
                    dismissButton = {
                        if (loginState is LoginState.Error) {
                            Text(
                                text = "Cancel",
                                color = ListenBrainzTheme.colorScheme.text,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        loginState = LoginState.Idle
                                        clearTimeout()
                                    }
                            )
                        }
                    }
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                clearTimeout()
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ListenBrainzClient(
    modifier: Modifier,
    username: String,
    password: String,
    onLoad: (Resource<String>) -> Unit,
    onPageLoadStateChange: (Boolean, String?) -> Unit
) {
    val returnUrl = "https://listenbrainz.org/login"
    val url = "https://musicbrainz.org/login?returnto=$returnUrl"

    // Use AndroidView to embed a WebView
    AndroidView(
        modifier = modifier,
        factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            fun clearCookies() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                } else {
                    val cookieSyncManager = CookieSyncManager.createInstance(context)
                    cookieSyncManager.startSync()
                    val cookieManager: CookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookie()
                    cookieManager.removeSessionCookie()
                    cookieSyncManager.stopSync()
                    cookieSyncManager.sync()
                }
            }
            // Configure WebView
            settings.apply {
                javaScriptEnabled = true
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false
            }

            // Clear cookies
            clearCookies()

            webViewClient = ListenBrainzWebClient(
                onLoad = onLoad,
                onPageLoadStateChange = onPageLoadStateChange,
                username = username,
                password = password
            )

            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(url)
        }
    })
}
