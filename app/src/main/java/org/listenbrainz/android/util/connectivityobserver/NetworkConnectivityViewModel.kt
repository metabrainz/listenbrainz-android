package org.listenbrainz.android.util.connectivityobserver

import kotlinx.coroutines.flow.Flow

/**
 * Use `NetworkConnectivityViewModelImpl` for instantiating view-model inside activities.
 *
 * Use `MockNetworkConnectivityViewModel` for instantiating view-model inside tests.
 *
 * Use this interface as an identifier for passing the view-model.
 *
 * This interface exists such that we can use mock view-model easily for testing purposes for each
 * and every use of the network view-model.
*/
interface NetworkConnectivityViewModel {
    fun getNetworkStatus() : ConnectivityObserver.NetworkStatus
    
    fun getNetworkStatusFlow() : Flow<ConnectivityObserver.NetworkStatus>
}