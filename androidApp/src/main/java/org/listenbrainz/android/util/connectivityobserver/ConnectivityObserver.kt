package org.listenbrainz.android.util.connectivityobserver

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {
    
    fun observe() : Flow<NetworkStatus>
    
    enum class NetworkStatus {
        AVAILABLE, UNAVAILABLE, LOSING, LOST
    }
}