package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.ui.screens.onboarding.auth.login.LoginConsentScreenUIState
import org.listenbrainz.shared.repository.AppPreferences

class LoginConsentViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _consentScreenUIState = MutableStateFlow(LoginConsentScreenUIState())
    val uiState: StateFlow<LoginConsentScreenUIState> = _consentScreenUIState

    init {
        viewModelScope.launch {
            val cacheData = appPreferences.consentScreenDataCache.getFlow().first()
            if (cacheData.isNotEmpty()) {
                _consentScreenUIState.update { uIState ->
                    uIState.copy(
                        data = cacheData,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun changeConsentScreenUIState(consentScreenUIState: LoginConsentScreenUIState) {
        _consentScreenUIState.update {
            consentScreenUIState
        }
        if (consentScreenUIState.data?.isNotEmpty() == true) {
            viewModelScope.launch {
                appPreferences.consentScreenDataCache.set(consentScreenUIState.data)
            }
        }
    }

    fun onLoadConsentScreen() {
        _consentScreenUIState.update { it ->
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }
        _consentScreenUIState.value.webView?.loadUrl("https://listenbrainz.org/login")
    }
}
