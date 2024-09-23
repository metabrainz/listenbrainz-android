package org.listenbrainz.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.util.Constants
import javax.inject.Inject

@HiltViewModel
class FeaturesViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    application: Application,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {

    private val _loginStatusFlow: MutableStateFlow<Int> = MutableStateFlow(Constants.Strings.STATUS_LOGGED_OUT)
    val loginStatusFlow: StateFlow<Int> = _loginStatusFlow.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
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