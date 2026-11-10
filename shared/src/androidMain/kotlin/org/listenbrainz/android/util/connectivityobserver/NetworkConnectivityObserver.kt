package org.listenbrainz.android.util.connectivityobserver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/** [NetworkConnectivityObserver] :
 *
 * Use this check if the device is connected to the internet or not.
 *
 * Usage for Compose activities :
 * ```
 *      val connectivityObserver = NetworkConnectivityObserver(applicationContext)
 *      val status by connectivityObserver.observe().collectAsState(
 *           initial = NetworkStatus.UNAVAILABLE
 *      )
 *      // Do something with status
 * ```
 * Usage for Non-Compose Activities / Classes(ViewModel) :
 *```
 *      val connectivityObserver = NetworkConnectivityObserver(context)
 *      connectivityObserver.observe().onEach {
 *          // Do Something
 *      }.launchIn{lifecycleScope}      // or viewModelScope
 * ```
 * */
class NetworkConnectivityObserver(
    context: Context
) : ConnectivityObserver {
    
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    
    override fun observe() : Flow<ConnectivityObserver.NetworkStatus> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectivityObserver.NetworkStatus.AVAILABLE) }
                }
    
                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.NetworkStatus.UNAVAILABLE) }
                }
                
                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(ConnectivityObserver.NetworkStatus.LOSING) }
                }
    
                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.NetworkStatus.LOST) }
                }
            }
            
            // Registering our callback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(callback)
            }
            else{
                connectivityManager.registerNetworkCallback(
                    NetworkRequest.Builder().build(),
                    callback
                )
            }

            // To cleanup callbacks when the user exits the activity.
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}