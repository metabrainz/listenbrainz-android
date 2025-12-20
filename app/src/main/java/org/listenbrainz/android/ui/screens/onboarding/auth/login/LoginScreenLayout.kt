package org.listenbrainz.android.ui.screens.onboarding.auth.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthPasswordField
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthUsernameField
import org.listenbrainz.android.ui.screens.onboarding.auth.createaccount.RefreshSection
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingSupportButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night

@Composable
fun LoginScreenLayout(
    uiState: LoginUIState,
    error: String?,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    webViewContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .alpha(
                    if (uiState.loginInState == LoginState.ShowingGDPRConsentPrompt ||
                        uiState.loginInState == LoginState.ShowingOAuthAuthorizationPrompt
                    ) 1f else 0f
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(
                Modifier
                    .statusBarsPadding()
                    .height(100.dp)
            )
            Text(
                if (uiState.loginInState == LoginState.ShowingGDPRConsentPrompt) "GDPR Consent Required" else "OAuth Authorization Required",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.loginInState == LoginState.ShowingGDPRConsentPrompt)
                    "To continue, please review and accept the GDPR consent form provided by MusicBrainz."
                else
                    "To continue, please authorize ListenBrainz to access your MusicBrainz account.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .height(if (uiState.loginInState == LoginState.ShowingGDPRConsentPrompt) 750.dp else 600.dp)
                    .width(372.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))

            ) {
                webViewContent()
            }
            Spacer(modifier = Modifier.height(16.dp))
            RefreshSection(onRefreshClick = onRefreshClick)
            Spacer(modifier = Modifier.height(32.dp))
        }
        if (uiState.loginInState != LoginState.ShowingGDPRConsentPrompt && uiState.loginInState != LoginState.ShowingOAuthAuthorizationPrompt) {
            Box(modifier = Modifier.fillMaxSize()) {
                LoginCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    username = uiState.username,
                    password = uiState.password,
                    error = error,
                    isLoading = isLoading,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onLoginClick = onLoginClick,
                    onCreateAccountClick = onCreateAccountClick,
                    onRefreshClick = onRefreshClick
                )
            }
        }
        OnboardingBackButton(
            modifier = Modifier
                .statusBarsPadding()
                .padding(
                    top = 8.dp, start = 8.dp
                )
        )
        OnboardingSupportButton(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        )

    }
}

@Composable
private fun LoginCard(
    username: String,
    password: String,
    error: String?,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
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
            val autoFillManager = LocalAutofillManager.current
            LoginHeader()
            Spacer(modifier = Modifier.height(32.dp))
            LoginForm(
                username = username,
                password = password,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onLoginClick = onLoginClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            ForgotCredentialsSection()
            Spacer(modifier = Modifier.height(24.dp))
            ErrorSection(error = error)
            LoginButton(
                isLoading = isLoading,
                onLoginClick = {
                    autoFillManager?.commit()
                    onLoginClick()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            RefreshSection(
                textColor = ListenBrainzTheme.colorScheme.text.copy(0.7f),
                onRefreshClick = onRefreshClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            CreateAccountSection(onCreateAccountClick)
        }
    }
}


@Composable
private fun LoginHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.musicbrainz_logo),
            contentDescription = "MusicBrainz Logo",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign in to your MusicBrainz account",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoginForm(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val passwordFocusRequester = remember { FocusRequester() }

    Column {
        AuthUsernameField(
            username = username,
            onUsernameChange = onUsernameChange,
            onNext = { passwordFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthPasswordField(
            password = password,
            onPasswordChange = onPasswordChange,
            focusRequester = passwordFocusRequester,
            onDone = onLoginClick
        )
    }
}


@Composable
private fun ForgotCredentialsSection() {
    val isLightTheme = !isSystemInDarkTheme()
    val linkColor = if (!isLightTheme) lb_purple_night else lb_purple

    val annotatedText = buildAnnotatedString {
        append("Forgot your ")

        withLink(
            link = LinkAnnotation.Url(
                "https://musicbrainz.org/lost-username",
                TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("username")
        }

        append(" or ")

        withLink(
            link = LinkAnnotation.Url(
                "https://musicbrainz.org/lost-password",
                TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("password")
        }

        append(" ?")
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
        ),
    )
}

@Composable
private fun ErrorSection(error: String?) {
    if (!error.isNullOrEmpty()) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun LoginButton(
    isLoading: Boolean,
    onLoginClick: () -> Unit
) {
    OnboardingYellowButton(
        modifier = Modifier.fillMaxWidth(),
        text = if (!isLoading) "Log in" else "Loading...",
        isEnabled = !isLoading,
        onClick = onLoginClick
    )
}

@Composable
private fun CreateAccountSection(onCreateAccountClick: () -> Unit) {
    val isLightTheme = !isSystemInDarkTheme()
    val linkColor = if (!isLightTheme) lb_purple_night else lb_purple
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
            ),
            text = buildAnnotatedString {
                append("New to ListenBrainz? ")
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "CREATE_ACCOUNT",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                fontWeight = FontWeight.Bold
                            )
                        ),
                        linkInteractionListener = {
                            onCreateAccountClick()
                        }
                    )
                ) {
                    append("Create an account")
                }

            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenLayoutPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginConsentScreen))
        LoginScreenLayout(
            uiState = LoginUIState(),
            error = null,
            isLoading = false,
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onCreateAccountClick = {}, // Add dummy lambda for preview
            onRefreshClick = {}
        )
    }
}