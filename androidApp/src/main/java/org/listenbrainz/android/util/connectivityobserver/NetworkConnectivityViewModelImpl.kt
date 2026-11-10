package org.listenbrainz.android.util.connectivityobserver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow

/**
 * NOTE: Use [NetworkConnectivityViewModel] for passing this view-model into functions.*/
class NetworkConnectivityViewModelImpl (application: Application) : AndroidViewModel(application), NetworkConnectivityViewModel {
    
    // Network Checking variables
    private val connectivityObserver = NetworkConnectivityObserver(application.applicationContext)
    
    private var networkStatusFlow:
            Flow<ConnectivityObserver.NetworkStatus>
            = connectivityObserver.observe()
    
    
    // Internet Connectivity Functions
    
    override fun getNetworkStatusFlow() : Flow<ConnectivityObserver.NetworkStatus> {
        return networkStatusFlow
    }
}