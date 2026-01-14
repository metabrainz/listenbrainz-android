package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.listenbrainz.shared.repository.AppPreferences

class SettingsViewModel(
    val appPreferences: AppPreferences,
): ViewModel() {

    private val _logoutStatus = MutableStateFlow<Boolean?>(null)
    val logoutStatus: StateFlow<Boolean?> = _logoutStatus.asStateFlow()

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