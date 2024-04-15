package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val appPreferences: AppPreferences
) : ViewModel() {

    fun version(): String {
        return appPreferences.version
    }
}