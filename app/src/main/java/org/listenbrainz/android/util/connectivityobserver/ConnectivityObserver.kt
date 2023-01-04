package org.listenbrainz.android.util.connectivityobserver

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {
    
    fun observe() : Flow<NetworkStatus>
    
    enum class NetworkStatus{
        Available, Unavailable, Losing, Lost
    }
}