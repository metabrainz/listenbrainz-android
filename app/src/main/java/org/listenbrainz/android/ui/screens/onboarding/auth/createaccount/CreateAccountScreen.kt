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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.screens.onboarding.auth.CreateAccountWebViewClient
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = "CreateAccountScreen"
private const val TIMEOUT = 60

// Sealed class to represent all possible account creation states
sealed class CreateAccountState {
    data object Idle : CreateAccountState()
    data class Loading(val message: String) : CreateAccountState()
    data object SubmittingForm : CreateAccountState()
    data object CreatingAccount : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
    data class Success(val message: String) : CreateAccountState()
}

@Composable
fun ListenBrainzCreateAccountScreen(onBackPress: ()-> Unit) {
    val scope = rememberCoroutineScope()

    var createAccountState by remember { mutableStateOf<CreateAccountState>(CreateAccountState.Idle) }
    var screenState by remember { mutableStateOf(CreateAccountScreenState.IDLE) }
    var username by remember { mutableStateOf("hemangmishra") }
    var email by remember { mutableStateOf("23bcs103@iiitdmj.ac.in") }
    var password by remember { mutableStateOf("hemangmishra") }
    var confirmPassword by remember { mutableStateOf("hemangmishra") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createAccountTimeoutJob by remember { mutableStateOf<Job?>(null) }

    val validateEmail = { email: String ->
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val validatePasswordMatch = { password: String, confirmPassword: String ->
        password == confirmPassword
    }

    val startTimeout = {
        createAccountTimeoutJob?.cancel()
        createAccountTimeoutJob = scope.launch {
            Log.d(TAG, "Starting account creation timer")
            repeat(TIMEOUT) {
                delay(1.seconds)
                if (createAccountState is CreateAccountState.Success || createAccountState is CreateAccountState.Error) {
                    return@launch
                }
                Log.d(TAG, "Account creation timer tick: ${it + 1} seconds")
            }
            if (createAccountState !is CreateAccountState.Success && screenState == CreateAccountScreenState.SHOWING_CAPTCHA) {
                Log.d(TAG, "Account creation timeout")
                createAccountState = CreateAccountState.Error("Account creation timed out. Please try again.")
                screenState = CreateAccountScreenState.IDLE
            }
        }
    }

    val clearTimeout = {
        Log.d(TAG, "Account creation timer cleared")
        createAccountTimeoutJob?.cancel()
        createAccountTimeoutJob = null
    }

    Box(modifier = Modifier.fillMaxSize()) {

        CreateAccountScreenLayout(
            username = username,
            password = password,
            confirmPassword = confirmPassword,
            email = email,
            error = if (createAccountState is CreateAccountState.Error) (createAccountState as CreateAccountState.Error).message else errorMessage,
            isLoading = createAccountState is CreateAccountState.Loading || createAccountState is CreateAccountState.CreatingAccount ||
                       createAccountState is CreateAccountState.SubmittingForm,
            screenState = screenState,
            captchaContent = {
                CreateAccountWebViewClient(
                    modifier = Modifier,
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    onLoad = { resource ->
                        Log.d(TAG, "Load state: ${createAccountState}, data: ${resource.data}, error: ${resource.error}")

                        if (createAccountState !is CreateAccountState.Error) {
                            when {
                                resource.isSuccess -> {
                                    clearTimeout()
                                    createAccountState = CreateAccountState.Success(resource.data ?: "Account created successfully!")

                                    scope.launch {
                                        delay(1500.milliseconds)
                                        screenState = CreateAccountScreenState.EMAIL_VERIFICATION
                                        createAccountState = CreateAccountState.Idle
                                    }
                                }

                                resource.isFailed -> {
                                    createAccountState = CreateAccountState.Error(
                                        resource.error?.actualResponse?.takeIf {
                                            it != "null"
                                        } ?: "Account creation failed"
                                    )
                                    screenState = CreateAccountScreenState.IDLE
                                    clearTimeout()
                                }

                                resource.isLoading -> {
                                    createAccountState = when {
                                        createAccountState == CreateAccountState.SubmittingForm -> CreateAccountState.CreatingAccount
                                        createAccountState !is CreateAccountState.Loading -> CreateAccountState.Loading("Connecting...")
                                        else -> createAccountState
                                    }
                                }
                            }
                        }
                    },
                    onPageLoadStateChange = { isLoading, message ->
                        if (isLoading && createAccountState !is CreateAccountState.Error) {
                            createAccountState = CreateAccountState.Loading(message ?: "Loading...")
                        }
                    }
                )
            },
            onUsernameChange = { username = it },
            onCreateAccountClick = {
                errorMessage = null

                when {
                    username.isBlank() -> {
                        errorMessage = "Username cannot be empty"
                        return@CreateAccountScreenLayout
                    }
                    email.isBlank() -> {
                        errorMessage = "Email cannot be empty"
                        return@CreateAccountScreenLayout
                    }
                    !validateEmail(email) -> {
                        errorMessage = "Please enter a valid email address"
                        return@CreateAccountScreenLayout
                    }
                    password.isBlank() -> {
                        errorMessage = "Password cannot be empty"
                        return@CreateAccountScreenLayout
                    }
                    confirmPassword.isBlank() -> {
                        errorMessage = "Please confirm your password"
                        return@CreateAccountScreenLayout
                    }
                    !validatePasswordMatch(password, confirmPassword) -> {
                        errorMessage = "Passwords do not match"
                        return@CreateAccountScreenLayout
                    }
                }


//                startTimeout()
//
//                createAccountState = CreateAccountState.SubmittingForm
                screenState = CreateAccountScreenState.SHOWING_CAPTCHA
            },
            onPasswordChange = { password = it },
            onConfirmPasswordChange = { confirmPassword = it },
            onEmailChange = { email = it },
            onVerificationCompleteClick = {
                screenState = CreateAccountScreenState.IDLE
                createAccountState = CreateAccountState.Idle
                onBackPress()
            },
            onPressBackInVerificationState = {
                screenState = CreateAccountScreenState.IDLE
                createAccountState = CreateAccountState.Idle
            },
            modifier = Modifier,
        )

        if (createAccountState !is CreateAccountState.Idle && screenState != CreateAccountScreenState.SHOWING_CAPTCHA) {
            val showDialog = createAccountState is CreateAccountState.Loading ||
                            createAccountState is CreateAccountState.CreatingAccount ||
                            createAccountState is CreateAccountState.Error ||
                            createAccountState is CreateAccountState.Success ||
                            createAccountState is CreateAccountState.SubmittingForm

            if (showDialog) {
                AlertDialog(
                    containerColor = ListenBrainzTheme.colorScheme.background,
                    onDismissRequest = {
                        if (createAccountState is CreateAccountState.Error) {
                            createAccountState = CreateAccountState.Idle
                            clearTimeout()
                        }
                    },
                    title = {
                        Text(
                            text = when (createAccountState) {
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
                        when (createAccountState) {
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
                                        text = when (createAccountState) {
                                            is CreateAccountState.Loading -> (createAccountState as CreateAccountState.Loading).message
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
                                    text = (createAccountState as CreateAccountState.Error).message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                            is CreateAccountState.Success -> {
                                Text(
                                    text = (createAccountState as CreateAccountState.Success).message,
                                    color = ListenBrainzTheme.colorScheme.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                            else -> {}
                        }
                    },
                    confirmButton = {
                        if (createAccountState is CreateAccountState.Error) {
                            Text(
                                text = "Try Again",
                                color = ListenBrainzTheme.colorScheme.text,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        createAccountState = CreateAccountState.Idle
                                        clearTimeout()
                                    }
                            )
                        }
                    },
                    properties = androidx.compose.ui.window.DialogProperties(
                        dismissOnBackPress = createAccountState is CreateAccountState.Error,
                        dismissOnClickOutside = createAccountState is CreateAccountState.Error
                    )
                )
            }
        }
    }
}
