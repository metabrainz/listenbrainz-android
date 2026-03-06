package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.util.Constants

class FeaturesViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _loginStatusFlow: MutableStateFlow<Int> = MutableStateFlow(Constants.Strings.STATUS_LOGGED_OUT)
    val loginStatusFlow: StateFlow<Int> = _loginStatusFlow.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.getLoginStatusFlow()
                .stateIn(this)
                .collectLatest {
                    _loginStatusFlow.emit(it)
                }
        }
    }
    fun loginStatus(): Int {
        return loginStatusFlow.value
    }

    fun isNotificationServiceAllowed(): Boolean = appPreferences.isNotificationServiceAllowed

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            appPreferences.onboardingCompleted.set(true)
        }
    }

    suspend fun isOnboardingCompleted(): Boolean = withContext(Dispatchers.IO) {
        appPreferences.onboardingCompleted.get()
    }

    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        appPreferences.isUserLoggedIn()
    }
}
