package org.listenbrainz.android.ui.screens.profile

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getActivity
import org.listenbrainz.android.viewmodel.ListensViewModel


@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel = ViewModelProvider(this)[ListensViewModel::class.java]
        setContent {
            ListenBrainzTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // FIXME: Security certificate warning in API 24 and below.
                    ListenBrainzLogin(viewModel)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ListenBrainzLogin(viewModel: ListensViewModel) {
    val url = "https://listenbrainz.org/login"
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current.getActivity()
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AndroidView(factory = {
            WebView(it).apply {
    
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
                
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = ListenBrainzWebClient { token ->
                    // Token is not null or empty.
                    coroutineScope.launch {
                        viewModel.saveUserDetails(token)
                        activity?.finish()
                    }
                }
                clearCookies()
                settings.javaScriptEnabled = true
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                loadUrl(url)
            }
        }, update = {
            it.loadUrl(url)
        })
    }
}
