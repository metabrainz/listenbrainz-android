package org.listenbrainz.android.ui.screens.onboarding.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun AuthUsernameField(
    username: String,
    onUsernameChange: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    contentType: ContentType = ContentType.Username,
    showPublicVisibilityNote: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    Column(modifier = modifier) {
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
            modifier = Modifier
                .fillMaxWidth()
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .semantics { this.contentType = contentType },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = ListenBrainzTheme.colorScheme.text,
                selectionColors = TextSelectionColors(
                    handleColor = ListenBrainzTheme.colorScheme.text,
                    backgroundColor = ListenBrainzTheme.colorScheme.text
                ),
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

        if (showPublicVisibilityNote) {
            Text(
                text = "Your username is publicly visible",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AuthEmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    contentType: ContentType = ContentType.EmailAddress,
) {
    Column(modifier = modifier) {
        Text(
            text = "Email",
            style = MaterialTheme.typography.bodyMedium,
            color = ListenBrainzTheme.colorScheme.text,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = {
                Text(
                    text = "Enter your email",
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics { this.contentType = contentType },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = ListenBrainzTheme.colorScheme.text,
                selectionColors = TextSelectionColors(
                    handleColor = ListenBrainzTheme.colorScheme.text,
                    backgroundColor = ListenBrainzTheme.colorScheme.text
                ),
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
fun AuthPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    contentType: ContentType = ContentType.Password,
    onDone: (() -> Unit)? = null,
    label: String = "Password",
    placeholder: String = "Enter your password",
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
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
                    text = placeholder,
                    color = ListenBrainzTheme.colorScheme.text.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics { this.contentType = contentType },
            singleLine = true,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = ListenBrainzTheme.colorScheme.text,
                selectionColors = TextSelectionColors(
                    handleColor = ListenBrainzTheme.colorScheme.text,
                    backgroundColor = ListenBrainzTheme.colorScheme.text
                ),
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
                imeAction = if (onNext != null) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = onNext?.let { { it() } },
                onDone = onDone?.let { { it() } }
            )
        )
    }
}
