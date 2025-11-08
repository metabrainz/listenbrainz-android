package org.listenbrainz.android.ui.screens.onboarding.auth

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.listenbrainz.android.ui.screens.profile.CreateAccountWebClient
import org.listenbrainz.android.util.Resource

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CreateAccountWebViewClient(
    modifier: Modifier = Modifier,
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    onLoad: (Resource<String>) -> Unit,
    onPageLoadStateChange: (Boolean, String?) -> Unit
) {
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

                webViewClient = CreateAccountWebClient(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    onLoad = onLoad,
                    onPageLoadStateChange = onPageLoadStateChange
                )

                loadUrl("https://musicbrainz.org/register?returnto=https://listenbrainz.org/")
            }
        }
    )

}
