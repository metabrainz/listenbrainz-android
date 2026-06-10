package org.listenbrainz.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.LogSubmitter

data class SettingsUiState(
    val logoutStatus: Boolean?= null,
    val isSubmittingLogs: Boolean = false
)

class SettingsViewModel(
    val appPreferences: AppPreferences,
    private val logSubmitter: LogSubmitter,
    private val logger: Log = Log
): ViewModel() {

    private val _logoutStatus = MutableStateFlow<Boolean?>(null)

    private val _submittingLogs = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        _logoutStatus,
        _submittingLogs
    ){ flows->
        SettingsUiState(
            logoutStatus = flows[0] as? Boolean,
            isSubmittingLogs = flows[1] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )

    fun logSubmit(){
        if(_submittingLogs.value){
            return
        }
        viewModelScope.launch {
            _submittingLogs.value = true
            try {
                logSubmitter.submitLogs()
            } catch(e:Exception){
                logger.e("Unable to submit logs: $e")
            }
            finally {
                _submittingLogs.value = false
            }
        }

    }

    fun logout() {
        viewModelScope.launch {
            // Execute the logout operation and capture the result
            val result = appPreferences.logoutUser()
            // Emit the result through the StateFlow
            _logoutStatus.emit(result)
        }
    }

    fun version(): String {
        return appPreferences.version
    }
}