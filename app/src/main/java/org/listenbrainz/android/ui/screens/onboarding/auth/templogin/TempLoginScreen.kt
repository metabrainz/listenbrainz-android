package org.listenbrainz.android.ui.screens.onboarding.auth.templogin

import android.content.res.Configuration
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.musicbrainz_logo
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthPasswordField
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.shared.resources.ic_listenbrainz_logo
import org.listenbrainz.shared.resources.ic_listenbrainz_logo_icon
import org.listenbrainz.shared.resources.listens_icon
import org.listenbrainz.shared.ui.screens.onboarding.auth.templogin.TempLoginState
import org.listenbrainz.shared.ui.screens.onboarding.auth.templogin.TempLoginUiState
import org.listenbrainz.shared.viewmodel.TempLoginViewModel

private const val TOKEN_SETTINGS_URL = "https://listenbrainz.org/settings/"

@Composable
fun TempLoginScreen(
    modifier: Modifier = Modifier,
    onLoginFinished: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        TempLoginCard(
            modifier = modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            onLoginFinished = onLoginFinished
        )

        OnboardingBackButton(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 8.dp)
        )
    }
}

@Composable
fun TempLoginCard(
    modifier: Modifier = Modifier,
    onLoginFinished: () -> Unit
) {
    val viewmodel = koinViewModel<TempLoginViewModel>()
    val uiState by viewmodel.uiState.collectAsState()

    TempLoginCard(
        modifier = modifier,
        uiState = uiState,
        onTokenChange = viewmodel::setToken,
        onSubmitToken = { viewmodel.submitToken { onLoginFinished() } }
    )
}

@Composable
fun TempLoginCard(
    modifier: Modifier = Modifier,
    uiState: TempLoginUiState,
    onTokenChange: (String) -> Unit,
    onSubmitToken: () -> Unit,
) {
    TokenCard(
        modifier = modifier,
        token = uiState.token,
        error = uiState.errorMessage,
        isVerifying = uiState.loginState is TempLoginState.VerifyingToken,
        onTokenChange = onTokenChange,
        onSubmitToken = onSubmitToken,
    )
}

@Composable
private fun TokenCard(
    token: String,
    error: String?,
    isVerifying: Boolean,
    onTokenChange: (String) -> Unit,
    onSubmitToken: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokenFocusRequester = remember { FocusRequester() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding(),
        colors = CardDefaults.cardColors(
            containerColor = ListenBrainzTheme.colorScheme.background.copy(alpha = 0.75f)
        ),
        shape = ListenBrainzTheme.shapes.listenCardSmall
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TokenHeader()

            Spacer(Modifier.height(32.dp))

            AuthPasswordField(
                password = token,
                onPasswordChange = onTokenChange,
                focusRequester = tokenFocusRequester,
                onDone = onSubmitToken,
                label = "User token",
                placeholder = "Paste your ListenBrainz user token"
            )

            Spacer(Modifier.height(16.dp))

            TokenHelpSection()

            Spacer(Modifier.height(24.dp))

            if (!error.isNullOrEmpty()) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OnboardingYellowButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (isVerifying) "Verifying..." else "Log in",
                isEnabled = !isVerifying && token.isNotBlank(),
                onClick = onSubmitToken
            )
        }
    }
}

@Composable
private fun TokenHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(Res.drawable.ic_listenbrainz_logo_icon),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Log in with a token",
            style = MaterialTheme.typography.headlineMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Paste the user token from your ListenBrainz account",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TokenHelpSection() {
    val isLightTheme =  !isSystemInDarkTheme()
    val linkColor = if (!isLightTheme) lb_purple_night else lb_purple

    Text(
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
        ),
        text = buildAnnotatedString {
            append("You can find your token in your ")
            withLink(
                link = LinkAnnotation.Url(
                    TOKEN_SETTINGS_URL,
                    TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            ) {
                append("ListenBrainz settings")
            }
        }
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TempLoginScreenLayoutPreview() {
    ListenBrainzTheme {
        TempLoginCard(
            uiState = TempLoginUiState(),
            onTokenChange = {},
            onSubmitToken = {}
        )
    }
}
