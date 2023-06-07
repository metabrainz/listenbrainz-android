package org.listenbrainz.android.ui.screens.profile

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import org.listenbrainz.android.viewmodel.ListensViewModel

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val viewModel = ViewModelProvider(this)[ListensViewModel::class.java]
        setContent {
            ListenBrainzTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ListenBrainzLogin(viewModel)
                }
            }
        }

    }
    

}

@Composable
fun ListenBrainzLogin(viewModel: ListensViewModel) {
    val url = "https://listenbrainz.org/login"
    val coroutineScope = rememberCoroutineScope()
    val activity = (LocalContext.current as? Activity)
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = ListenBrainzWebClient { token ->
                    viewModel.appPreferences.lbAccessToken = token
                    coroutineScope.launch {
                        viewModel.appPreferences.username = viewModel.retrieveUsername(token)
                        activity?.finish()
                    }
                }
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }, update = {
            it.loadUrl(url)
        })
    }
}
