package org.listenbrainz.shared.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.PlatformContext
import org.listenbrainz.shared.util.Log
import org.listenbrainz.shared.util.LogSubmitter

class SettingsViewModel(
    val appPreferences: AppPreferences,
    private val logSubmitter: LogSubmitter,
    private val logger: Log = Log
): ViewModel() {

    private val _logoutStatus = MutableStateFlow<Boolean?>(null)
    val logoutStatus: StateFlow<Boolean?> = _logoutStatus.asStateFlow()

    private val _submittingLogs = MutableStateFlow(false)
    val submittingLogs: StateFlow<Boolean> = _submittingLogs.asStateFlow()

    fun logSubmit(context : PlatformContext){
        if(_submittingLogs.value){
            return
        }
        viewModelScope.launch {
            _submittingLogs.value = true
            try {
                logSubmitter.submitLogs(context)
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