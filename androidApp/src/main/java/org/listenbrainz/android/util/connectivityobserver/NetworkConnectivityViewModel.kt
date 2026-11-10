package org.listenbrainz.android.util.connectivityobserver

import kotlinx.coroutines.flow.Flow

/**
 * Use [NetworkConnectivityViewModelImpl] for instantiating view-model inside activities.
 *
 * Use `MockNetworkConnectivityViewModel` (inside *sharedTest* module) for instantiating view-model inside tests.
 *
 * Use this interface as an identifier for passing the view-model.
 *
 * This interface exists such that we can use mock view-models easily for testing purposes for each
 * and every use of the network view-model.
 */
interface NetworkConnectivityViewModel {
    
    fun getNetworkStatusFlow(): Flow<ConnectivityObserver.NetworkStatus>
}