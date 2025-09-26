package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ListenBrainzCreateAccountScreen() {
    var uiState by remember {
        mutableStateOf(CreateAccountUIState())
    }
    CreateAccountScreenLayout(
        username = uiState.username,
        password = uiState.password,
        confirmPassword = uiState.confirmPassword,
        email = uiState.email,
        error = uiState.errorMessage,
        isLoading = false,
        onUsernameChange = {
            uiState = uiState.copy(username = it)
        },
        onCreateAccountClick = {
            //TODO
        },
        onPasswordChange = {
            uiState = uiState.copy(password = it)
        },
        onConfirmPasswordChange = {
            uiState = uiState.copy(confirmPassword = it)
        },
        onEmailChange = {
            uiState = uiState.copy(email = it)
        },
        modifier = Modifier,
    )
}