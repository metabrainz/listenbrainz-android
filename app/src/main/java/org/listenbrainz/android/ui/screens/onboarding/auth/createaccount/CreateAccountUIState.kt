package org.listenbrainz.android.ui.screens.onboarding.auth.createaccount

data class CreateAccountUIState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val isVerificationScreenVisible: Boolean = false
)