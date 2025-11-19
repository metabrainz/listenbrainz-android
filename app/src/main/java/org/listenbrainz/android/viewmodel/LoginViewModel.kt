package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import com.limurse.logger.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginState
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginUIState
import org.listenbrainz.android.util.Resource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


class LoginViewModel() : BaseViewModel<LoginUIState>() {
    private companion object {
        const val TIMEOUT_SECONDS = 60
    }
    private val TAG = "LoginViewModel"
    private val loginUIState = MutableStateFlow(LoginUIState())

    private var loginInTimeOut: Job? = null


    override val uiState: StateFlow<LoginUIState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<LoginUIState> {
        return combine(
            loginUIState,
        ) {
            it[0]
        }.stateIn(viewModelScope, initialValue = LoginUIState(), started = SharingStarted.Lazily)
    }

    fun setUsername(username: String) {
        loginUIState.update {
            it.copy(
                username = username,
                errorMessage = null
            )
        }
    }

    fun setPassword(password: String) {
        loginUIState.update {
            it.copy(
                password = password,
                errorMessage = null)
        }
    }

    fun onRefreshClick(){
        reloadWebView()
        loginUIState.update {
            it.copy(
                loginInState = LoginState.LoadingLoginForm,
                errorMessage = null
            )
        }
    }

    private fun reloadWebView() {
        loginUIState.update {
            it.copy(reloadTrigger = it.reloadTrigger + 1)
        }
    }

    fun onLoginFormLoaded() {
        loginUIState.update {
            it.copy(
                loginInState = LoginState.Idle
            )
        }
    }

    fun showGDPRConsentPrompt(){
        viewModelScope.launch {
            delay(2500)
            loginUIState.update {
                it.copy(
                    loginInState = LoginState.ShowingGDPRConsentPrompt
                )
            }
        }
    }

    fun showOAuthAuthorizationPrompt(){
        viewModelScope.launch {
            delay(2500)
            loginUIState.update {
                it.copy(
                    loginInState = LoginState.ShowingOAuthAuthorizationPrompt
                )
            }
        }
    }

    fun submitForm(){
        if(uiState.value.username.isBlank() || uiState.value.password.isBlank()){
            loginUIState.update {
                it.copy(
                    loginInState = LoginState.Error("Username and Password cannot be empty"),
                    errorMessage = "Username and Password cannot be empty"
                )
            }
            return
        }
        startTimeout()
        loginUIState.update {
            it.copy(
                submitFormTrigger = true,
                loginInState = LoginState.SubmittingCredentials
            )
        }
    }

    fun onLoad(resource: Resource<String>, validateAndSaveUserDetails: suspend (Resource<String>)-> Resource<Unit>, onLoginFinished: ()-> Unit) {
        Logger.d(TAG, "Load state: ${uiState.value.loginInState}, data: ${resource.data}, error: ${resource.error}")
        val state = loginUIState.value.loginInState
        if (state !is LoginState.Error){
            when {
                resource.isSuccess -> {
                    loginUIState.update {
                        it.copy(loginInState = LoginState.VerifyingToken)
                    }
                    viewModelScope.launch {
                        val validationResult = validateAndSaveUserDetails(resource)
                        loginUIState.update {
                            it.copy(loginInState = if(validationResult.status == Resource.Status.SUCCESS){
                                clearTimeout()
                                LoginState.Success("Login successful")
                            } else {
                                clearTimeout()
                                LoginState.Error(validationResult.error?.actualResponse?.takeIf { res->
                                    res != "null"
                                } ?: "Login failed during validation")
                            })
                        }
                        if(validationResult.status == Resource.Status.SUCCESS) {
                            delay(1500.milliseconds)
                            cleanup()
                            onLoginFinished()
                        }
                    }
                }

                resource.isFailed -> {
                    clearTimeout()
                    loginUIState.update {
                        it.copy(
                            loginInState = LoginState.Error(resource.error?.actualResponse?.takeIf { res->
                                res != "null"
                            } ?: "Login failed")
                        )
                    }
                    viewModelScope.launch {
                        delay(1.seconds)
                        onRefreshClick()
                    }
                }


            }
        }

    }

    fun startTimeout() {
        loginInTimeOut?.cancel()
        loginInTimeOut = viewModelScope.launch {
            repeat(TIMEOUT_SECONDS) {
                delay(1.seconds)
                val state = loginUIState.value
                if (state.loginInState is LoginState.Success ||
                    state.loginInState is LoginState.Error) {
                    return@launch
                }
            }

            val state = loginUIState.value
            if (state.loginInState !is LoginState.Success &&
                state.loginInState !is LoginState.Error &&
                state.loginInState !is LoginState.Idle &&
                state.loginInState !is LoginState.ShowingGDPRConsentPrompt &&
                state.loginInState !is LoginState.ShowingOAuthAuthorizationPrompt
                ) {
                loginUIState.update {
                    it.copy(
                        loginInState = LoginState.Error("Login timed out. Please try again."),
                    )
                }
            }
        }
    }

    fun onDismissDialogInErrorState() {
        loginUIState.update {
            it.copy(
                loginInState = LoginState.Idle,
                errorMessage = null
            )
        }
        onRefreshClick()
        clearTimeout()
    }

    fun resetSubmitFormTrigger() {
        loginUIState.update {
            it.copy(
                submitFormTrigger = false
            )
        }
    }

    fun cleanup(){
        clearTimeout()
        loginUIState.update {
            LoginUIState()
        }
    }

    private fun clearTimeout() {
        loginInTimeOut?.cancel()
        loginInTimeOut = null
    }

    override fun onCleared() {
        super.onCleared()
        clearTimeout()
    }
}