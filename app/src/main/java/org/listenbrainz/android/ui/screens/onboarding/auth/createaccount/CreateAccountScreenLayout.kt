package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalContext
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
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthEmailField
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthPasswordField
import org.listenbrainz.android.ui.screens.onboarding.auth.AuthUsernameField
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingBackButton
import org.listenbrainz.android.ui.screens.onboarding.introduction.OnboardingSupportButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.viewmodel.CreateAccountUIState
import org.listenbrainz.android.viewmodel.CreateAccountScreenState

@Composable
fun CreateAccountScreenLayout(
    username: String,
    password: String,
    confirmPassword: String,
    email: String,
    error: String?,
    isLoading: Boolean,
    uiState: CreateAccountUIState,
    screenState: CreateAccountScreenState = CreateAccountScreenState.IDLE,
    captchaContent: @Composable () -> Unit = {},
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCreateAccountClick: () -> Unit,
    onVerificationCompleteClick: () -> Unit = {},
    onPressBackInVerificationState: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (screenState == CreateAccountScreenState.EMAIL_VERIFICATION) {
        BackHandler {
            onPressBackInVerificationState()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .alpha(if (screenState == CreateAccountScreenState.SHOWING_CAPTCHA) 1f else 0.0f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(
                Modifier
                    .statusBarsPadding()
                    .height(100.dp)
            )
            Text(
                "CAPTCHA Verification",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please complete the verification below to continue",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .width(372.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))

            ) {
                captchaContent()
                if (!uiState.captchaSetupComplete) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = ListenBrainzTheme.shapes.listenCardSmall
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingAnimation()
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            RefreshSection(onRefreshClick = onRefreshClick)
            Spacer(modifier = Modifier.height(32.dp))
        }

        AnimatedContent(targetState = screenState) { state ->
            when (state) {
                CreateAccountScreenState.EMAIL_VERIFICATION -> {
                    EmailVerificationCard(
                        email = email,
                        onVerificationCompleteClick = onVerificationCompleteClick,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    )
                }

                CreateAccountScreenState.SHOWING_CAPTCHA -> {

                }

                CreateAccountScreenState.IDLE -> {
                    CreateAccountCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                        username = username,
                        password = password,
                        confirmPassword = confirmPassword,
                        email = email,
                        error = error,
                        isLoading = isLoading,
                        onUsernameChange = onUsernameChange,
                        onPasswordChange = onPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onEmailChange = onEmailChange,
                        onCreateAccountClick = onCreateAccountClick,
                        onRefreshClick = onRefreshClick,
                        uiState = uiState
                    )
                }
            }
        }
        OnboardingBackButton(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 8.dp)
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
private fun CreateAccountCard(
    username: String,
    password: String,
    confirmPassword: String,
    email: String,
    error: String?,
    isLoading: Boolean,
    uiState: CreateAccountUIState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCreateAccountClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                CreateAccountHeader()
                Spacer(modifier = Modifier.height(32.dp))
                CreateAccountForm(
                    username = username,
                    password = password,
                    confirmPassword = confirmPassword,
                    email = email,
                    onUsernameChange = onUsernameChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onEmailChange = onEmailChange,
                    onCreateAccountClick = onCreateAccountClick
                )
                Spacer(modifier = Modifier.height(24.dp))
                CodeOfConductSection()
                Spacer(modifier = Modifier.height(24.dp))
                ErrorSection(error = error)
                CreateAccountButton(
                    isLoading = isLoading,
                    onCreateAccountClick = {
                        autoFillManager?.commit()
                        onCreateAccountClick()
                    },
                    isEnabled = uiState.captchaSetupComplete
                )
                Spacer(modifier = Modifier.height(8.dp))
                RefreshSection(
                    onRefreshClick = onRefreshClick,
                    textColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun CreateAccountHeader() {
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
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create an account with MusicBrainz",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CreateAccountForm(
    username: String,
    password: String,
    confirmPassword: String,
    email: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Column {
        AuthUsernameField(
            username = username,
            onUsernameChange = onUsernameChange,
            contentType = ContentType.NewUsername,
            onNext = { emailFocusRequester.requestFocus() },
            showPublicVisibilityNote = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthEmailField(
            email = email,
            onEmailChange = onEmailChange,
            focusRequester = emailFocusRequester,
            onNext = { passwordFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthPasswordField(
            password = password,
            onPasswordChange = onPasswordChange,
            focusRequester = passwordFocusRequester,
            onNext = { confirmPasswordFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthPasswordField(
            password = confirmPassword,
            onPasswordChange = onConfirmPasswordChange,
            focusRequester = confirmPasswordFocusRequester,
            contentType = ContentType.NewPassword,
            onDone = onCreateAccountClick,
            label = "Confirm Password",
            placeholder = "Confirm your password",
        )
    }
}

@Composable
private fun CodeOfConductSection() {
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
                append("Please review the ")
                withLink(
                    link = LinkAnnotation.Url(
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ),
                        url = "https://musicbrainz.org/doc/Code_of_Conduct"
                    )
                ) {
                    append("MusicBrainz Code of Conduct")
                }
                append(" before creating an account")
            },
        )
    }
}

@Composable
fun RefreshSection(
    textColor: Color = Color.White.copy(0.9f),
    onRefreshClick: () -> Unit
) {
    val isLightTheme = !isSystemInDarkTheme()
    val linkColor = if (!isLightTheme) lb_purple_night else lb_purple

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor
            ),
            text = buildAnnotatedString {
                append("Facing any issues? ")
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "refresh",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ),
                        linkInteractionListener = {
                            onRefreshClick()
                        }
                    )
                ) {
                    append("Refresh")
                }
            },
        )
    }
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
private fun CreateAccountButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onCreateAccountClick: () -> Unit
) {
    val context = LocalContext.current
    OnboardingYellowButton(
        modifier = Modifier.fillMaxWidth(),
        text = "Create Account",
        isEnabled = !isLoading && isEnabled,
        onClick = onCreateAccountClick,
        onClickWhileDisabled = {
            Toast.makeText(context, "Loading CAPTCHA, please wait...", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
private fun EmailVerificationCard(
    email: String,
    onVerificationCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Image(
                    painter = painterResource(id = R.drawable.musicbrainz_logo),
                    contentDescription = "MusicBrainz Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Email Verification Required",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ListenBrainzTheme.colorScheme.text,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Email must be verified to create a ListenBrainz account with a MusicBrainz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We've sent a verification link to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ListenBrainzTheme.colorScheme.text,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Open Inbox button
                TextButton(
                    onClick = {
                        val intent = Intent.createChooser(
                            Intent(Intent.ACTION_MAIN)
                                .addCategory(Intent.CATEGORY_APP_EMAIL)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                },
                            "Open email app"
                        )
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val gmailIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                setPackage("com.google.android.gm")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(gmailIntent)
                            } catch (e: Exception) {
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Open Inbox",
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) lb_purple_night else lb_purple,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Verification Complete button
                OnboardingYellowButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Verification Complete",
                    onClick = onVerificationCompleteClick
                )
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreateAccountScreenLayoutPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginConsentScreen))
        CreateAccountScreenLayout(
            username = "",
            password = "",
            confirmPassword = "",
            email = "",
            error = null,
            isLoading = false,
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onEmailChange = {},
            onCreateAccountClick = {},
            uiState = CreateAccountUIState()
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmailVerificationScreenLayoutPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginConsentScreen))
        CreateAccountScreenLayout(
            username = "",
            password = "",
            confirmPassword = "",
            email = "user@example.com",
            error = null,
            isLoading = false,
            screenState = CreateAccountScreenState.EMAIL_VERIFICATION,
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onEmailChange = {},
            onCreateAccountClick = {},
            onVerificationCompleteClick = {},
            uiState = CreateAccountUIState()
        )
    }
}
