package org.listenbrainz.shared.ui.screens.onboarding.auth.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.json.JSONTokener
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme

actual fun clearWebLoginSession() {
    CookieManager.getInstance().removeAllCookies(null)
    CookieManager.getInstance().flush()
}

@Composable
actual fun WebLoginDialog(onTokenFound: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        WebLoginScreen(onTokenFound = onTokenFound, onClose = onDismiss)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebLoginScreen(
    onTokenFound: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnTokenFound by rememberUpdatedState(onTokenFound)
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler {
        val view = webView
        if (view != null && view.canGoBack()) view.goBack() else onClose()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        private var isTokenFound = false

                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            isLoading = false
                            if (isTokenFound || !WebLogin.isListenBrainzPage(url)) return
                            view.evaluateJavascript(WebLogin.TOKEN_SCRIPT) { result ->
                                // The result is JSON encoded
                                val token = (JSONTokener(result).nextValue() as? String)
                                    ?.takeIf { it.isNotBlank() }
                                if (token != null && !isTokenFound) {
                                    isTokenFound = true
                                    currentOnTokenFound(token)
                                }
                            }
                        }
                    }
                    loadUrl(WebLogin.LOGIN_URL)
                    webView = this
                }
            },
            onRelease = { it.destroy() }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}
