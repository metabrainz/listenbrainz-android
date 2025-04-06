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
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils.LaunchedEffectUnit
import org.listenbrainz.android.viewmodel.ListensViewModel
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ListenBrainzTheme {
                ListenBrainzLogin(
                    modifier = Modifier
                        .background(ListenBrainzTheme.colorScheme.background)
                        .safeDrawingPadding(),
                    onLoginFinished = ::finish
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenBrainzLogin(
    modifier: Modifier = Modifier,
    onLoginFinished: () -> Unit
) {
    val viewModel = hiltViewModel<ListensViewModel>()
    var loadState by remember {
        mutableStateOf<Resource<String>?>(null)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // FIXME: Security certificate warning in API 24 and below.
        ListenBrainzClient {
            loadState = it
        }

        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            targetState = loadState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { state ->
            when (state?.status) {
                Resource.Status.LOADING, Resource.Status.SUCCESS -> {
                    var isTokenValidRes by remember {
                        mutableStateOf<Resource<Unit>?>(null)
                    }

                    if (state.isSuccess) {
                        LaunchedEffectUnit {
                            isTokenValidRes = Resource.loading()
                            isTokenValidRes = viewModel.saveUserDetails(state.data!!)
                        }
                    }

                    BasicAlertDialog(
                        onDismissRequest = {},
                        content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        ListenBrainzTheme.colorScheme.background,
                                        MaterialTheme.shapes.large
                                    )
                                    .padding(24.dp), // From M3 source code
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (isTokenValidRes?.status) {
                                    null -> {
                                        LoadingAnimation()
                                    }
                                    Resource.Status.LOADING -> {
                                        LoadingAnimation()
                                        Text(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            text = "Verifying token...",
                                            color = ListenBrainzTheme.colorScheme.text,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Resource.Status.SUCCESS -> {
                                        LaunchedEffectUnit {
                                            delay(1.5.seconds)
                                            onLoginFinished()
                                        }

                                        Text(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            text = "Login successful!",
                                            color = ListenBrainzTheme.colorScheme.text,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Resource.Status.FAILED -> {
                                        LaunchedEffectUnit {
                                            Log.e("LoginActivity", "Token validation failed: ${isTokenValidRes?.error?.toString()}")
                                            delay(2.seconds)
                                            onLoginFinished()
                                        }

                                        Text(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            text = "Login failed.\nReason: ${isTokenValidRes?.error?.toast ?: "Unknown error"}",
                                            color = ListenBrainzTheme.colorScheme.text,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                Resource.Status.FAILED -> {
                    LaunchedEffectUnit {
                        delay(1.5.seconds)
                        onLoginFinished()
                    }

                    AlertDialog(
                        containerColor = ListenBrainzTheme.colorScheme.background,
                        onDismissRequest = {},
                        confirmButton = {},
                        text = {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = "Something went wrong, please try again later.",
                                color = ListenBrainzTheme.colorScheme.text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
                else -> {}
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ListenBrainzClient(
    onLoad: (Resource<String>) -> Unit,
) {
    val url = "https://listenbrainz.org/login"
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
            webViewClient = ListenBrainzWebClient(onLoad = onLoad)
            clearCookies()
            settings.javaScriptEnabled = true
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            loadUrl(url)
        }
    }, update = {
        it.loadUrl(url)
    })
}
