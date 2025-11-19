package org.listenbrainz.android.ui.screens.onboarding.auth.login

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.profile.ListenBrainzWebClient
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.ListensViewModel
import org.listenbrainz.android.viewmodel.LoginViewModel

const val TAG = "ListenBrainzLogin"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenBrainzLogin(
    modifier: Modifier = Modifier,
    onCreateAccountClicked: () -> Unit,
    onLoginFinished: () -> Unit
) {
    val listensViewModel = hiltViewModel<ListensViewModel>()
    val vm = hiltViewModel<LoginViewModel>()
    val uiState by vm.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        // Main login UI
        LoginScreenLayout(
            uiState = uiState,
            onUsernameChange = vm::setUsername,
            onPasswordChange = vm::setPassword,
            error = uiState.errorMessage,
            isLoading = uiState.loginInState == LoginState.LoadingLoginForm ||
                    uiState.loginInState == LoginState.Loading ||
                    uiState.loginInState == LoginState.SubmittingCredentials ||
                    uiState.loginInState == LoginState.VerifyingToken,
            onCreateAccountClick = onCreateAccountClicked,
            onLoginClick = vm::submitForm,
            webViewContent = {
                ListenBrainzClient(
                    modifier = Modifier,
                    vm = vm,
                    uiState = uiState,
                    callbacks = LoginCallbacks(
                        onLoad = { resource ->
                            vm.onLoad(
                                resource,
                                onLoginFinished = onLoginFinished,
                                validateAndSaveUserDetails = {
                                    listensViewModel.validateAndSaveUserDetails(it.data!!)
                                }
                            )
                        },
                        onMusicBrainzLoginFormLoaded = vm::onLoginFormLoaded,
                        showGDPRConsentPrompt = vm::showGDPRConsentPrompt,
                        showOAuthAuthorizationPrompt = vm::showOAuthAuthorizationPrompt
                    )
                )
            },
            onRefreshClick = vm::onRefreshClick
        )

        if (uiState.loginInState !is LoginState.Idle && uiState.loginInState != LoginState.ShowingGDPRConsentPrompt && uiState.loginInState != LoginState.ShowingOAuthAuthorizationPrompt) {
            val loginState = uiState.loginInState
            val showDialog = loginState is LoginState.Loading ||
                    loginState is LoginState.VerifyingToken ||
                    loginState is LoginState.Error ||
                    loginState is LoginState.Success ||
                    loginState is LoginState.SubmittingCredentials
//
            if (showDialog) {
                AlertDialog(
                    containerColor = ListenBrainzTheme.colorScheme.background,
                    onDismissRequest = vm::onDismissDialogInErrorState,
                    title = {
                        Text(
                            text = when (loginState) {
                                is LoginState.Error -> "Login Error"
                                is LoginState.Success -> "Success"
                                else -> "Signing In"
                            },
                            color = ListenBrainzTheme.colorScheme.text,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        when (loginState) {
                            is LoginState.Loading,
                            is LoginState.SubmittingCredentials,
                            is LoginState.VerifyingToken -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LoadingAnimation()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = when (loginState) {
                                            is LoginState.SubmittingCredentials -> "Submitting credentials..."
                                            is LoginState.VerifyingToken -> "Verifying token..."
                                            else -> "Loading..."
                                        },
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            is LoginState.Error -> {
                                Text(
                                    text = loginState.message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }

                            is LoginState.Success -> {
                                Text(
                                    text = loginState.message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {}
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
                                        vm.onDismissDialogInErrorState()
                                    }
                            )
                        }
                    },
                    dismissButton = {
                        Text(
                            text = "Cancel",
                            color = ListenBrainzTheme.colorScheme.text,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    vm.onDismissDialogInErrorState()
                                }
                        )
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = true
                    )
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ListenBrainzClient(
    modifier: Modifier,
    callbacks: LoginCallbacks,
    uiState: LoginUIState,
    vm: LoginViewModel
) {
    val returnUrl = "https://listenbrainz.org/login"
    val url = "https://musicbrainz.org/login?returnto=$returnUrl"

    var webViewClientRef by remember { mutableStateOf<ListenBrainzWebClient?>(null) }

    LaunchedEffect(uiState.submitFormTrigger) {
        if (uiState.submitFormTrigger) {
            webViewClientRef?.submitLoginForm(
                username = uiState.username,
                password = uiState.password
            )
            vm.resetSubmitFormTrigger()
        }
    }

    // Use AndroidView to embed a WebView
    key(uiState.reloadTrigger) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Configure WebView
                    settings.apply {
                        javaScriptEnabled = true
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                    }

                    // Clear cookies
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()

                    val client = ListenBrainzWebClient(
                        callbacks = callbacks
                    )
                    webViewClientRef = client
                    webViewClient = client

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    loadUrl(url)
                    //Start timeout
                    vm.startTimeout()
                }
            })
    }
}
