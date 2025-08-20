package org.listenbrainz.android.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.InstallSource
import org.listenbrainz.android.repository.appupdates.AppUpdatesRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class AppUpdatesViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val appUpdatesRepository: AppUpdatesRepository,
    application: Application
) : AndroidViewModel(application) {

    init {
        checkInstallSource()
    }

    private fun checkInstallSource() {
        viewModelScope.launch {
            val currentInstallSource = appPreferences.installSource.get()

            if (currentInstallSource == InstallSource.NOT_CHECKED) {
                val detectedSource = appUpdatesRepository.detectInstallSource(getApplication())
                Log.d("AppUpdatesViewModel", "Detected install source: $detectedSource")
                appPreferences.installSource.set(detectedSource)
            } else {
                Log.d("AppUpdatesViewModel", "Install source already checked: $currentInstallSource")
            }
        }
    }
}
