package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.listenbrainz.android.repository.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(appPreferences: AppPreferences): ViewModel() {

}