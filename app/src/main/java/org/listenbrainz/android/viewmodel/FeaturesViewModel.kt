package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants

class FeaturesViewModel(
    val appPreferences: AppPreferences
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
}