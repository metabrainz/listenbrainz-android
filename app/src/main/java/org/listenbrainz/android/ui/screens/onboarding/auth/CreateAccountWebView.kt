package org.listenbrainz.android.ui.screens.onboarding.auth

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.listenbrainz.android.ui.screens.onboarding.auth.createaccount.CreateAccountClientCallbacks
import org.listenbrainz.android.ui.screens.onboarding.auth.createaccount.CreateAccountWebAppInterface
import org.listenbrainz.android.ui.screens.profile.CreateAccountWebClient
import org.listenbrainz.android.viewmodel.CreateAccountViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CreateAccountWebViewClient(
    modifier: Modifier = Modifier,
    viewModel: CreateAccountViewModel,
    callbacks: CreateAccountClientCallbacks
) {
    val uiState by viewModel.uiState.collectAsState()
    var webClientRef by remember { mutableStateOf<CreateAccountWebClient?>(null) }

    LaunchedEffect(uiState.submitFormTrigger) {
        if (uiState.submitFormTrigger) {
            webClientRef?.submitRegistrationForm(uiState.credentials)
            viewModel.resetSubmitFormTrigger()
        }
    }

    key(uiState.reloadTrigger) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        setSupportMultipleWindows(false)
                        javaScriptCanOpenWindowsAutomatically = false
                    }

                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()

                    clearCache(true)
                    clearHistory()

                    val client = CreateAccountWebClient(
                        callbacks = callbacks
                    )
                    webViewClient = client
                    webClientRef = client

                    addJavascriptInterface(
                        CreateAccountWebAppInterface(
                            onCaptchaVerificationCompleted = {
                                // Triggering form submission from ViewModel after captcha is verified
                                viewModel.submitForm()
                            }
                        ), "AndroidInterface"
                    )

                    viewModel.setCaptchaNotComplete()

                    loadUrl("https://musicbrainz.org/register?returnto=https://listenbrainz.org/")
                }
            }
        )
    }
}
