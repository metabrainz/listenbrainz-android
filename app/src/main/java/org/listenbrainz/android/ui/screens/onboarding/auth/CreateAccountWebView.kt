package org.listenbrainz.android.ui.screens.onboarding.auth

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountWebView(onFinished: ()-> Unit) {
    val url = "https://musicbrainz.org/register"
    var isPageLoading by remember {
        mutableStateOf(true)
    }
    Box(modifier = Modifier.fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = CreateAccountWebViewClient(
                        onAccountCreated = onFinished,
                        onPageLoadStateChange = {
                            if (it != isPageLoading)
                                isPageLoading = it
                        })
                    loadUrl(url)
                }
            },
            update = {
                it.loadUrl(url)
            }
        )
        AnimatedVisibility(modifier = Modifier.fillMaxSize(), visible = isPageLoading) {
            LoadingDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(modifier: Modifier = Modifier){
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    ListenBrainzTheme.colorScheme.background,
                    MaterialTheme.shapes.large
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingAnimation()
        }
    }
}