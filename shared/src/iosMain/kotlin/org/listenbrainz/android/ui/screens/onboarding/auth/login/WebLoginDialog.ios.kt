package org.listenbrainz.android.ui.screens.onboarding.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.ui.screens.onboarding.auth.login.WebLogin
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.distantPast
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

actual fun clearWebLoginSession() {
    WKWebsiteDataStore.defaultDataStore().removeDataOfTypes(
        dataTypes = WKWebsiteDataStore.allWebsiteDataTypes(),
        modifiedSince = NSDate.distantPast,
        completionHandler = {}
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WebLoginDialog(onTokenFound: (String) -> Unit, onDismiss: () -> Unit) {
    val currentOnTokenFound by rememberUpdatedState(onTokenFound)
    // WKWebView only holds its delegate weakly
    val delegate = remember { TokenNavigationDelegate { currentOnTokenFound(it) } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        UIKitView(
            factory = {
                WKWebView(frame = CGRectZero.readValue(), configuration = WKWebViewConfiguration()).apply {
                    navigationDelegate = delegate
                    NSURL.URLWithString(WebLogin.LOGIN_URL)?.let { loadRequest(NSURLRequest.requestWithURL(it)) }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(ListenBrainzTheme.colorScheme.background)
                .safeDrawingPadding()
        )
    }
}

private class TokenNavigationDelegate(
    private val onTokenFound: (String) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
    private var isTokenFound = false

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        if (isTokenFound || !WebLogin.isListenBrainzPage(webView.URL?.absoluteString)) return
        webView.evaluateJavaScript(WebLogin.TOKEN_SCRIPT) { result, _ ->
            val token = (result as? String)?.takeIf { it.isNotBlank() }
            if (token != null && !isTokenFound) {
                isTokenFound = true
                onTokenFound(token)
            }
        }
    }
}
