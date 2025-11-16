package org.listenbrainz.android.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Sealed class to represent all possible account creation states
sealed class CreateAccountState {
    data object Idle : CreateAccountState()
    data class Loading(val message: String) : CreateAccountState()
    data object SubmittingForm : CreateAccountState()
    data object CreatingAccount : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
    data class Success(val message: String) : CreateAccountState()
}

enum class CreateAccountScreenState {
    IDLE,
    SHOWING_CAPTCHA,
    EMAIL_VERIFICATION
}

data class CreateAccountUIState(
    val credentials: CreateAccountCredentials = CreateAccountCredentials(),
    val reloadTrigger: Int = 0,
    val captchaSetupComplete: Boolean = false,
    val submitFormTrigger: Boolean = false,
    val screenState: CreateAccountScreenState = CreateAccountScreenState.IDLE,
    val createAccountState: CreateAccountState = CreateAccountState.Idle,
    val errorMessage: String? = null
)

data class CreateAccountCredentials(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class CreateAccountViewModel : ViewModel() {
    private companion object {
        const val TIMEOUT_SECONDS = 60
    }

    private val _uiState = MutableStateFlow(CreateAccountUIState())
    val uiState = _uiState.asStateFlow()

    private var createAccountTimeoutJob: Job? = null

    fun setUsername(username: String) {
        _uiState.update {
            it.copy(credentials = it.credentials.copy(username = username))
        }
    }

    fun setEmail(email: String) {
        _uiState.update {
            it.copy(credentials = it.credentials.copy(email = email))
        }
    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(credentials = it.credentials.copy(password = password))
        }
    }

    fun setConfirmPassword(confirmPassword: String) {
        _uiState.update {
            it.copy(credentials = it.credentials.copy(confirmPassword = confirmPassword))
        }
    }

    fun setCaptchaSetupComplete() {
        viewModelScope.launch {
            delay(700) // To ensure that WebView has completely loaded the captcha
            _uiState.update {
                it.copy(captchaSetupComplete = true)
            }
        }
    }

    fun setCaptchaNotComplete() {
        _uiState.update {
            it.copy(captchaSetupComplete = false)
        }
    }

    fun reloadWebView() {
        _uiState.update {
            it.copy(reloadTrigger = it.reloadTrigger + 1, captchaSetupComplete = false)
        }
    }

    fun submitForm() {
        _uiState.update {
            it.copy(submitFormTrigger = true,
                createAccountState = CreateAccountState.SubmittingForm)
        }
        startTimeout()
    }

    fun resetSubmitFormTrigger() {
        _uiState.update {
            it.copy(submitFormTrigger = false)
        }
    }

    fun onCreateAccountClick() {
        val credentials = _uiState.value.credentials

        // Validate fields
        when {
            credentials.username.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Username cannot be empty") }
                return
            }
            credentials.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Email cannot be empty") }
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(credentials.email).matches() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
                return
            }
            credentials.password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Password cannot be empty") }
                return
            }
            credentials.confirmPassword.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please confirm your password") }
                return
            }
            credentials.password != credentials.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                return
            }
        }

        // Clear error and show captcha screen
        _uiState.update {
            it.copy(
                errorMessage = null,
                screenState = CreateAccountScreenState.SHOWING_CAPTCHA
            )
        }
    }

    fun onWebViewLoadStateChange(isLoading: Boolean, message: String?) {
        if (isLoading && _uiState.value.createAccountState !is CreateAccountState.Error) {
            _uiState.update {
                it.copy(createAccountState = CreateAccountState.Loading(message ?: "Loading..."))
            }
        }
    }

    fun onAccountCreationSuccess(message: String) {
        clearTimeout()
        _uiState.update {
            it.copy(createAccountState = CreateAccountState.Success(message))
        }

        viewModelScope.launch {
            delay(1500.milliseconds)
            _uiState.update {
                it.copy(
                    screenState = CreateAccountScreenState.EMAIL_VERIFICATION,
                    createAccountState = CreateAccountState.Idle
                )
            }
        }
    }

    fun onAccountCreationFailed(errorMessage: String) {
        clearTimeout()
        reloadWebView()
        _uiState.update {
            it.copy(
                createAccountState = CreateAccountState.Error(errorMessage),
                screenState = CreateAccountScreenState.IDLE
            )
        }
    }

    fun onAccountCreationLoading() {
        val currentState = _uiState.value.createAccountState
        _uiState.update {
            it.copy(
                createAccountState = when {
                    currentState == CreateAccountState.SubmittingForm -> CreateAccountState.CreatingAccount
                    currentState !is CreateAccountState.Loading -> CreateAccountState.Loading("Connecting...")
                    else -> currentState
                }
            )
        }
    }

    fun dismissError() {
        clearTimeout()
        _uiState.update {
            it.copy(createAccountState = CreateAccountState.Idle)
        }
    }

    fun onVerificationComplete() {
        _uiState.update {
            it.copy(
                screenState = CreateAccountScreenState.IDLE,
                createAccountState = CreateAccountState.Idle
            )
        }
    }

    fun onBackInVerificationState() {
        _uiState.update {
            it.copy(
                screenState = CreateAccountScreenState.IDLE,
                createAccountState = CreateAccountState.Idle
            )
        }
    }

    private fun startTimeout() {
        createAccountTimeoutJob?.cancel()
        createAccountTimeoutJob = viewModelScope.launch {
            repeat(TIMEOUT_SECONDS) {
                delay(1.seconds)
                val state = _uiState.value
                if (state.createAccountState is CreateAccountState.Success ||
                    state.createAccountState is CreateAccountState.Error) {
                    return@launch
                }
            }

            val state = _uiState.value
            if (state.createAccountState !is CreateAccountState.Success &&
                state.screenState == CreateAccountScreenState.SHOWING_CAPTCHA) {
                _uiState.update {
                    it.copy(
                        createAccountState = CreateAccountState.Error("Account creation timed out. Please try again."),
                        screenState = CreateAccountScreenState.IDLE
                    )
                }
            }
        }
    }

    private fun clearTimeout() {
        createAccountTimeoutJob?.cancel()
        createAccountTimeoutJob = null
    }

    override fun onCleared() {
        super.onCleared()
        clearTimeout()
    }
}