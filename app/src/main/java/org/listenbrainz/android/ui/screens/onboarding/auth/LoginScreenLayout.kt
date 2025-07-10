package org.listenbrainz.android.ui.screens.onboarding.auth

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.OnboardingScreenBackground
import org.listenbrainz.android.ui.components.OnboardingYellowButton
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.lb_purple_night
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction

@Composable
fun LoginScreenLayout(
    username: String,
    password: String,
    error: String?,
    isLoading: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginCard(
            username = username,
            password = password,
            error = error,
            isLoading = isLoading,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onLoginClick = onLoginClick,
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
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
                onLoginClick = onLoginClick
            )
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
        UsernameField(
            username = username,
            onUsernameChange = onUsernameChange,
            onNext = { passwordFocusRequester.requestFocus() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            password = password,
            onPasswordChange = onPasswordChange,
            focusRequester = passwordFocusRequester,
            onDone = onLoginClick
        )
    }
}

@Composable
private fun UsernameField(
    username: String,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column {
        Text(
            text = "Username",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            placeholder = {
                Text(
                    text = "Enter your username",
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ListenBrainzTheme.colorScheme.text,
                unfocusedTextColor = ListenBrainzTheme.colorScheme.text,
                focusedBorderColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f),
                unfocusedBorderColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.3f)
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            )
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onDone: () -> Unit
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = {
                Text(
                    text = "Enter your password",
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ListenBrainzTheme.colorScheme.text,
                unfocusedTextColor = ListenBrainzTheme.colorScheme.text,
                focusedBorderColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f),
                unfocusedBorderColor = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.3f)
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisibility) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                        tint = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f)
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            )
        )
    }
}

@Composable
private fun ForgotCredentialsSection() {
    val isLightTheme = !isSystemInDarkTheme()
    val linkColor = if (!isLightTheme) lb_purple_night else lb_purple

    val annotatedText = buildAnnotatedString {
        append("Forgot your ")

        withLink(link = LinkAnnotation.Url(
            "https://musicbrainz.org/lost-username",
            TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
        ))) {
                append("username")
        }

        append(" or ")

        withLink(link = LinkAnnotation.Url(
            "https://musicbrainz.org/lost-password",
            TextLinkStyles(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            ))) {
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
        text = "Log in",
        isEnabled = !isLoading,
        onClick = onLoginClick
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenLayoutPreview() {
    ListenBrainzTheme {
        OnboardingScreenBackground(backStack = rememberNavBackStack(NavigationItem.OnboardingScreens.LoginConsentScreen))
        LoginScreenLayout(
            username = "",
            password = "",
            error = null,
            isLoading = false,
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
        )
    }
}