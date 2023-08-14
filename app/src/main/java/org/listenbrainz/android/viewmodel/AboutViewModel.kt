package org.listenbrainz.android.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    application: Application,
) : AndroidViewModel(application) {

    fun version(): String {
        return appPreferences.version
    }
}