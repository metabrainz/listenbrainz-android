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
import org.listenbrainz.shared.ui.screens.onboarding.auth.templogin.TempLoginState
import org.listenbrainz.shared.ui.screens.onboarding.auth.templogin.TempLoginUiState
import org.listenbrainz.shared.util.Log

class TempLoginViewModel(
    private val listensRepository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val ioDispatcher: CoroutineDispatcher,
    private val logger: Log = Log
) : BaseViewModel<TempLoginUiState>() {

    private val TAG = "TempLoginViewModel"
    private val tempLoginUIState = MutableStateFlow(TempLoginUiState())

    override val uiState: StateFlow<TempLoginUiState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<TempLoginUiState> {
        return combine(tempLoginUIState)
        {
            it[0]
        }.stateIn(viewModelScope, initialValue = TempLoginUiState(), started = SharingStarted.Lazily)
    }

    fun setToken(token: String) {
        tempLoginUIState.update {
            it.copy(
                token = token,
                errorMessage = null,
            )
        }
    }
    private fun setError(message: String) {
        tempLoginUIState.update {
            it.copy(
                loginState = TempLoginState.Error(message),
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
        if (uiState.value.loginState is TempLoginState.VerifyingToken) {
            return
        }

        tempLoginUIState.update {
            it.copy(
                loginState = TempLoginState.VerifyingToken,
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
                    tempLoginUIState.update {
                        it.copy(loginState = TempLoginState.Success("Login successful"))
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
