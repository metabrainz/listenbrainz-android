package org.listenbrainz.android.ui.screens.onboarding.auth.login


data class LoginUIState(
    val username: String = "",
    val password: String = "",
    val reloadTrigger: Int = 0,
    val submitFormTrigger: Boolean = false,
    val loginInState: LoginState = LoginState.LoadingLoginForm,
    val errorMessage: String? = null
)

sealed class LoginState {
    data object LoadingLoginForm: LoginState()
    data object Idle : LoginState()
    data object Loading: LoginState()
    data object SubmittingCredentials : LoginState()
    data object ShowingOAuthAuthorizationPrompt: LoginState()
    data object ShowingGDPRConsentPrompt : LoginState()
    data object VerifyingToken : LoginState()
    data class Error(val message: String) : LoginState()
    data class Success(val message: String) : LoginState()
}
