package org.listenbrainz.android.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class CreateAccountUIState(
    val credentials: CreateAccountCredentials = CreateAccountCredentials(),
    val reloadTrigger: Int = 0,
    val captchaSetupComplete: Boolean = false,
)
data class CreateAccountCredentials(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

class CreateAccountViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUIState())
    val uiState = _uiState.asStateFlow()

    fun setCaptchaSetupComplete(){
        viewModelScope.launch {
            delay(700) // To ensure that WebView has completely loaded the captcha
            _uiState.update {
                it.copy(captchaSetupComplete = true)
            }
        }
    }

    fun setCaptchaNotComplete(){
        _uiState.update {
            it.copy(captchaSetupComplete = false)
        }
    }

    fun reloadWebView() {
        _uiState.update {
            it.copy(reloadTrigger = it.reloadTrigger + 1, captchaSetupComplete = false)
        }
    }
}