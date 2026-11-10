package org.listenbrainz.shared.ui.screens.onboarding.auth.login

data class LoginUiState(
    val token: String = "",
    val loginState: LoginState = LoginState.Initial,
    val errorMessage: String? = null
)

sealed class LoginState {
    data object Initial : LoginState()
    data object VerifyingToken : LoginState()

    data class Error(val message: String) : LoginState()

    data class Success(val message: String) : LoginState()
}
