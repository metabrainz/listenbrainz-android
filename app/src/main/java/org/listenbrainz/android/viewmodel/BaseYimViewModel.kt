package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel



abstract class BaseYimViewModel : ViewModel() {
    abstract fun getData()
}