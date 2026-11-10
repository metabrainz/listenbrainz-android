package org.listenbrainz.shared.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.ui.screens.onboarding.auth.login.LoginState
import org.listenbrainz.shared.ui.screens.onboarding.auth.login.LoginUiState
import org.listenbrainz.shared.util.Log

class LoginViewModel(
    private val listensRepository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Log = Log
) : BaseViewModel<LoginUiState>() {

    private val TAG = "LoginViewModel"
    private val loginUiState = MutableStateFlow(LoginUiState())

    override val uiState: StateFlow<LoginUiState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<LoginUiState> {
        return combine(loginUiState)
        {
            it[0]
        }.stateIn(viewModelScope, initialValue = LoginUiState(), started = SharingStarted.Lazily)
    }

    fun setToken(token: String) {
        loginUiState.update {
            it.copy(
                token = token,
                errorMessage = null,
            )
        }
    }
    private fun setError(message: String) {
        loginUiState.update {
            it.copy(
                loginState = LoginState.Error(message),
                errorMessage = message
            )
        }
    }

    /** Verifies and saves a token read from the website. */
    fun submitToken(token: String, onLoginFinished: () -> Unit) {
        setToken(token)
        submitToken(onLoginFinished)
    }

    fun submitToken(onLoginFinished: () -> Unit) {
        val token = uiState.value.token.trim()

        if (token.isBlank()) {
            setError("Token cannot be empty")
            return
        }
        if (uiState.value.loginState is LoginState.VerifyingToken) {
            return
        }

        loginUiState.update {
            it.copy(
                loginState = LoginState.VerifyingToken,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    listensRepository.validateToken(token)
                }
                val validation = result.data

                if (result.isSuccess && validation != null && validation.valid) {
                    appPreferences.username.set(validation.username ?: "")
                    appPreferences.lbAccessToken.set(token)
                    loginUiState.update {
                        it.copy(loginState = LoginState.Success("Login successful"))
                    }
                    onLoginFinished()
                } else {
                    val errorMessage = validation?.message?.takeIf { it.isNotBlank() }
                        ?: result.error?.actualResponse?.takeIf { it.isNotBlank() && it != "null" }
                        ?: "Invalid token. Please check and try again."
                    logger.e("Token validation failed: ${errorMessage}", tag = TAG)
                    setError(errorMessage)
                }
            } catch (e: Exception) {
                setError("Login failed: ${e.message}")
            }
        }
    }
}
