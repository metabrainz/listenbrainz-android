package org.listenbrainz.android.ui.screens.onboarding.auth.login

import android.content.res.Configuration
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.style.URLSpan
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.auth.ConsentWebViewClient
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingSupportButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.viewmodel.DashBoardViewModel

data class LoginConsentScreenUIState(
    val data: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val webView: WebView? = null,
)

@Composable
fun ConsentScreenDataInitializer(dashBoardViewModel: DashBoardViewModel){
    val uiState by dashBoardViewModel.consentScreenUIState.collectAsState()

    if(uiState.data == null) {
        //Setting up consent screen content
        AndroidView(
            modifier = Modifier.size(1.dp)
                .alpha(0.0f),
            factory = {
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
                    webViewClient = ConsentWebViewClient(
                        onLoadData = { text ->
                            dashBoardViewModel.changeConsentScreenUIState(
                                uiState.copy(
                                    data = text,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            )
                        },
                        onError = { error ->
                            dashBoardViewModel.changeConsentScreenUIState(
                                uiState.copy(
                                    isLoading = false,
                                    errorMessage = error
                                )
                            )
                        }
                    )
                    clearCookies()
                    loadUrl("https://listenbrainz.org/login")
                    settings.javaScriptEnabled = true
                    dashBoardViewModel.changeConsentScreenUIState(
                        uiState.copy(
                            webView = this
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun LoginConsentScreen(dashBoardViewModel: DashBoardViewModel,
                       onProceedToLoginScreen: () -> Unit) {
    val uiState by dashBoardViewModel.consentScreenUIState.collectAsState()
    LoginConsentScreenLayout(
        html = uiState.data ?: "",
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onClickNext = onProceedToLoginScreen,
        onRetry = {
            dashBoardViewModel.onLoadConsentScreen()
        }
    )
}

@Composable
private fun LoginConsentScreenLayout(
    html: String,
    isLoading: Boolean,
    errorMessage: String?,
    onClickNext: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier
                .statusBarsPadding()
                .height(100.dp))

            Text(
                text = "Sign in",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AnimatedContent(html.isNotEmpty()) {
                if (it) {
                    val paragraphs = parseHtmlToParagraphs(html)
                    Text(
                        text = htmlToAnnotatedString(
                            html = paragraphs[0],
                            lb_purple_night
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ListenBrainzTheme.colorScheme.background.copy(alpha = 0.75f)
                ),
                shape = ListenBrainzTheme.shapes.listenCardSmall
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    AnimatedVisibility(
                        visible = isLoading,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation()
                        }
                    }
                    AnimatedVisibility(
                        visible = !isLoading,
                    ) {
                        Column {
                            Text(
                                text = "Important!",
                                color = ListenBrainzTheme.colorScheme.text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (html.isNotEmpty()) {
                                val paragraphs = parseHtmlToParagraphs(html)
                                paragraphs.drop(2).forEach { paragraph ->
                                    Text(
                                        text = htmlToAnnotatedString(
                                            html = paragraph,
                                            linkColor = if (isSystemInDarkTheme()) lb_purple_night else lb_purple
                                        ),
                                        color = ListenBrainzTheme.colorScheme.text,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }
                            }

                            errorMessage?.let { error ->
                                Text(
                                    text = error,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            OnboardingYellowButton(
                text = if (errorMessage != null) "Retry" else "Sign In With MusicBrainz",
                isEnabled = !isLoading,
                onClick = if (errorMessage != null) onRetry else onClickNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
        OnboardingBackButton(modifier = Modifier
            .statusBarsPadding()
            .padding(top = 8.dp, start = 8.dp))

        OnboardingSupportButton(modifier = Modifier
            .statusBarsPadding()
            .align(Alignment.TopEnd)
            .padding(top = 8.dp , end = 8.dp)
        )
    }
}

private fun parseHtmlToParagraphs(html: String): List<String> {
    val unescapedHtml = html.removeSurrounding("\"")
        .replace("\\u003C", "<")
        .replace("\\u003E", ">")
        .replace("\\n", "\n")

    // Split by double newlines to separate paragraphs
    val paragraphs = unescapedHtml.split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return paragraphs
}

private fun htmlToAnnotatedString(html: String, linkColor: Color): AnnotatedString {
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
            if (!url.startsWith("http")) {
                url = "https://listenbrainz.org$url"
            }
            addStyle(
                style = SpanStyle(color = linkColor, fontWeight = FontWeight.Bold),
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


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginConsentScreenPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginConsentScreen))
        LoginConsentScreenLayout(
            html = "\"To sign in, use your MusicBrainz account, and authorize ListenBrainz to access your profile data.\\n\\nImportant!\\n\\nBy signing into ListenBrainz, you grant the MetaBrainz Foundation permission to include your listening history in data dumps we make publicly available under the \\u003Ca href=\\\"https://creativecommons.org/publicdomain/zero/1.0/\\\">CC0 license\\u003C/a>. None of your private information from your user profile will be included in these data dumps.\\n\\nFurthermore, you grant the MetaBrainz Foundation permission to process your listening history and include it in new open source tools such as recommendation engines that the ListenBrainz project is building. For details on processing your listening history, please see our \\u003Ca href=\\\"https://metabrainz.org/gdpr\\\">GDPR compliance statement\\u003C/a>.\\n\\nIn order to combat spammers and to be able to contact our users in case something goes wrong with the listen submission process, we now require an email address when creating a ListenBrainz account.\\n\\nIf after creating an account you change your mind about processing your listening history, you will need to \\u003Ca href=\\\"/settings/delete/\\\" data-discover=\\\"true\\\">delete your ListenBrainz account\\u003C/a>.\"",
            isLoading = false,
            errorMessage = null,
            onClickNext = {},
            onRetry = {}
        )
    }
}