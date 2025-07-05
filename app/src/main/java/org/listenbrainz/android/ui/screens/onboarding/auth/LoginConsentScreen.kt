package org.listenbrainz.android.ui.screens.onboarding.auth

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.style.URLSpan
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.viewinterop.AndroidView
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.theme.onboardingGradient

@Composable
fun LoginConsentScreen(onProceedToLoginScreen: () -> Unit) {
    var data by remember {
        mutableStateOf<String?>(null)
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    AndroidView(
        factory = {  
            WebView(it).apply {
                webViewClient = ConsentWebViewClient({ text ->
                    data = text
                    isLoading = false
                })
                loadUrl("https://listenbrainz.org/login")
                settings.javaScriptEnabled = true
            }
        }
    )
    LoginConsentScreenLayout(data ?: "",
        isLoading,
        onProceedToLoginScreen)
}

@Composable
private fun LoginConsentScreenLayout(
    html: String,
    isLoading: Boolean,
    onClickNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = onboardingGradient)
            .statusBarsPadding(),
    ) {
        LazyColumn() {
            item {

                Text(
                    htmlToAnnotatedString(
                        html = html,
                        linkColor = Color(0xFF1E88E5)
                    ),
                )

            }
            item {
                OnboardingYellowButton(
                    text = "Sign in MusicBrainz",
                    onClick = onClickNext
                )
            }
        }
    }
}

@Composable
fun htmlToAnnotatedString(html: String, linkColor: Color): AnnotatedString {
    val unescapedHtml = html.removeSurrounding("\"")
        .replace("\\u003C", "<")
        .replace("\\u003E", ">")
        .replace("\\n", "\n")
    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(unescapedHtml, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(unescapedHtml)
    } as Spanned
    return buildAnnotatedString {
        append(spanned.toString())
        val urlSpans = spanned.getSpans(0, spanned.length, URLSpan::class.java)
        urlSpans.forEach { urlSpan ->
            val start = spanned.getSpanStart(urlSpan)
            val end = spanned.getSpanEnd(urlSpan)
            var url = urlSpan.url.removeSurrounding("\\\"")
            if(!url.startsWith("http")){
                url = "https://listenbrainz.org$url"
            }
            addStyle(
                style = SpanStyle(color = linkColor),
                start = start,
                end = end
            )
            addLink(
                url = LinkAnnotation.Url(url),
                start = start,
                end = end
            )

        }
    }
}

