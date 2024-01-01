package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val appPreferences: AppPreferences,
): ViewModel() {
    
    fun version(): String {
        return appPreferences.version
    }
    
    fun logout() {
        viewModelScope.launch {
            appPreferences.logoutUser()
        }
    }
}