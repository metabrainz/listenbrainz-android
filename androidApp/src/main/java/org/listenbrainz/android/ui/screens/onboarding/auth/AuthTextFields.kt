package org.listenbrainz.android.ui.screens.onboarding.auth

import androidx.compose.foundation.layout.Column
import org.jetbrains.compose.resources.painterResource
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_visibility
import org.listenbrainz.shared.resources.ic_visibility_off
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
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

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
                        painter = painterResource(if (passwordVisibility) Res.drawable.ic_visibility_off else Res.drawable.ic_visibility),
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
