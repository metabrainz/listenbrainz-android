package org.listenbrainz.android.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CreateAccountCredentials(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

class CreateAccountViewModel: ViewModel() {
    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger = _reloadTrigger.asStateFlow()
    private val _formSubmissionTrigger = MutableStateFlow<CreateAccountCredentials?>(null)
    val formSubmissionTrigger = _formSubmissionTrigger.asStateFlow()

    fun submitRegistrationForm(
        credentials: CreateAccountCredentials
    ){
        _formSubmissionTrigger.value = credentials
    }

    fun resetFormSubmissionTrigger(){
        _formSubmissionTrigger.value = null
    }

    fun reloadWebView() {
        _reloadTrigger.value += 1
    }
}