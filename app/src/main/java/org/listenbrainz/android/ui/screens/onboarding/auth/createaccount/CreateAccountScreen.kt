package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.onboarding.auth.CreateAccountWebViewClient
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.CreateAccountState
import org.listenbrainz.android.viewmodel.CreateAccountScreenState
import org.listenbrainz.android.viewmodel.CreateAccountViewModel

private const val TAG = "CreateAccountScreen"

@Composable
fun ListenBrainzCreateAccountScreen(onBackPress: () -> Unit) {
    val vm: CreateAccountViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        CreateAccountScreenLayout(
            username = uiState.credentials.username,
            password = uiState.credentials.password,
            confirmPassword = uiState.credentials.confirmPassword,
            email = uiState.credentials.email,
            error = if (uiState.createAccountState is CreateAccountState.Error)
                (uiState.createAccountState as CreateAccountState.Error).message
            else uiState.errorMessage,
            isLoading = uiState.createAccountState is CreateAccountState.Loading ||
                    uiState.createAccountState is CreateAccountState.CreatingAccount ||
                    uiState.createAccountState is CreateAccountState.SubmittingForm,
            screenState = uiState.screenState,
            captchaContent = {
                CreateAccountWebViewClient(
                    modifier = Modifier,
                    callbacks = CreateAccountClientCallbacks(
                        onLoad = { resource ->
                            Log.d(TAG, "Load state: ${uiState.createAccountState}, data: ${resource.data}, error: ${resource.error}")

                            if (uiState.createAccountState !is CreateAccountState.Error) {
                                when {
                                    resource.isSuccess -> {
                                        vm.onAccountCreationSuccess(
                                            resource.data ?: "Account created successfully!"
                                        )
                                    }

                                    resource.isFailed -> {
                                        vm.onAccountCreationFailed(
                                            resource.error?.actualResponse?.takeIf {
                                                it != "null"
                                            } ?: "Account creation failed"
                                        )
                                    }

                                    resource.isLoading -> {
                                        vm.onAccountCreationLoading()
                                    }
                                }
                            }
                        },
                        onPageLoadStateChange = { isLoading, message ->
                            vm.onWebViewLoadStateChange(isLoading, message)
                        },
                        onCaptchaSetupComplete = {
                            vm.setCaptchaSetupComplete()
                        }
                    ),
                    viewModel = vm
                )
            },
            onUsernameChange = vm::setUsername,
            onCreateAccountClick = vm::onCreateAccountClick,
            onPasswordChange = vm::setPassword,
            onConfirmPasswordChange = vm::setConfirmPassword,
            onEmailChange = vm::setEmail,
            onVerificationCompleteClick = {
                vm.onVerificationComplete()
                onBackPress()
            },
            onPressBackInVerificationState = vm::onBackInVerificationState,
            onRefreshClick = vm::reloadWebView,
            modifier = Modifier,
            uiState = uiState
        )

        if (uiState.createAccountState !is CreateAccountState.Idle) {
            val showDialog = uiState.createAccountState is CreateAccountState.Loading ||
                    uiState.createAccountState is CreateAccountState.CreatingAccount ||
                    uiState.createAccountState is CreateAccountState.Error ||
                    uiState.createAccountState is CreateAccountState.Success ||
                    uiState.createAccountState is CreateAccountState.SubmittingForm

            if (showDialog) {
                AlertDialog(
                    containerColor = ListenBrainzTheme.colorScheme.background,
                    onDismissRequest = {
                        if (uiState.createAccountState is CreateAccountState.Error) {
                            vm.dismissError()
                        }
                    },
                    title = {
                        Text(
                            text = when (uiState.createAccountState) {
                                is CreateAccountState.Error -> "Account Creation Error"
                                is CreateAccountState.Success -> "Success"
                                else -> "Creating Account"
                            },
                            color = ListenBrainzTheme.colorScheme.text,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        when (uiState.createAccountState) {
                            is CreateAccountState.Loading,
                            is CreateAccountState.SubmittingForm,
                            is CreateAccountState.CreatingAccount -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    LoadingAnimation()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = when (uiState.createAccountState) {
                                            is CreateAccountState.Loading -> (uiState.createAccountState as CreateAccountState.Loading).message
                                            is CreateAccountState.SubmittingForm -> "Submitting registration form..."
                                            is CreateAccountState.CreatingAccount -> "Creating your account..."
                                            else -> "Loading..."
                                        },
                                        color = ListenBrainzTheme.colorScheme.text,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            is CreateAccountState.Error -> {
                                Text(
                                    text = (uiState.createAccountState as CreateAccountState.Error).message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is CreateAccountState.Success -> {
                                Text(
                                    text = (uiState.createAccountState as CreateAccountState.Success).message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                            else -> {}
                        }
                    },
                    confirmButton = {
                        if (uiState.createAccountState is CreateAccountState.Error) {
                            Text(
                                text = "Try Again",
                                color = ListenBrainzTheme.colorScheme.text,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        vm.dismissError()
                                    }
                            )
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = uiState.createAccountState is CreateAccountState.Error,
                        dismissOnClickOutside = uiState.createAccountState is CreateAccountState.Error
                    )
                )
            }
        }
    }
}
