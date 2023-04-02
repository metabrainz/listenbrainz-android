package org.listenbrainz.android.util.connectivityobserver

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * NOTE: Use [NetworkConnectivityViewModel] for passing this view-model into functions.*/
class NetworkConnectivityViewModelImpl (context: Context) : ViewModel(), NetworkConnectivityViewModel {
    
    // Network Checking variables
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private var networkStatus:
            MutableState<ConnectivityObserver.NetworkStatus>
            = mutableStateOf(ConnectivityObserver.NetworkStatus.Unavailable)   // initial value
    
    init {
        checkNetworkStatus()
    }
    
    // Internet Connectivity Functions
    private fun checkNetworkStatus(){
        connectivityObserver.observe().onEach {
            networkStatus.value = it
        }.launchIn(viewModelScope)
    }
    override fun getNetworkStatus() : ConnectivityObserver.NetworkStatus{
        return networkStatus.value
    }
    
    override fun getNetworkStatusFlow() : Flow<ConnectivityObserver.NetworkStatus> {
        return connectivityObserver.observe()
    }
}