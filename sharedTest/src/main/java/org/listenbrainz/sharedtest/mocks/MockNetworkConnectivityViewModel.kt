package org.listenbrainz.sharedtest.mocks

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel

class MockNetworkConnectivityViewModel (private val mockedNetworkStatus: ConnectivityObserver.NetworkStatus) : ViewModel(), NetworkConnectivityViewModel {
    
    override fun getNetworkStatus() : ConnectivityObserver.NetworkStatus {
        return mockedNetworkStatus
    }
    
    override fun getNetworkStatusFlow(): Flow<ConnectivityObserver.NetworkStatus> {
        return flow {
            emit(mockedNetworkStatus)
        }
    }
}