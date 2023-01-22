package org.listenbrainz.android.presentation.features.yim

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.listenbrainz.android.util.connectivityobserver.ConnectivityObserver
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityObserver

class YimNetworkViewModel (context: Context) : ViewModel() {
    
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
    fun getNetworkStatus() : ConnectivityObserver.NetworkStatus{
        return networkStatus.value
    }
}