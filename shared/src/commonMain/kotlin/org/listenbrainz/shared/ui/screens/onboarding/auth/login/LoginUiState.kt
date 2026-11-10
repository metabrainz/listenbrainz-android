package org.listenbrainz.shared.ui.screens.onboarding.auth.login

data class TempLoginUiState(
    val token: String = "",
    val loginState: TempLoginState = TempLoginState.Initial,
    val errorMessage: String? = null
)

sealed class TempLoginState {
    data object Initial : TempLoginState()
    data object VerifyingToken : TempLoginState()

    data class Error(val message: String) : TempLoginState()

    data class Success(val message: String) : TempLoginState()
}
