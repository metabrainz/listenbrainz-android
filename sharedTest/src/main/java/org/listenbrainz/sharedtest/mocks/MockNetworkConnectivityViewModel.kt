package org.listenbrainz.sharedtest.mocks

import androidx.lifecycle.ViewModel
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver

class MockNetworkConnectivityViewModel (private val mockedNetworkStatus: ConnectivityObserver.NetworkStatus) : ViewModel() {
    
    fun getNetworkStatus() : ConnectivityObserver.NetworkStatus {
        return mockedNetworkStatus
    }
}