package org.listenbrainz.android.presentation.features.yim

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.repository.YimRepositoryImpl
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.presentation.features.login.LoginSharedPreferences
import org.listenbrainz.android.util.ConnectivityObserver
import org.listenbrainz.android.util.NetworkConnectivityObserver
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class YimViewModel @Inject constructor(private val repository: YimRepository, @ApplicationContext context: Context) : ViewModel() {
    // Yim data resource
    private var resourceState:
            MutableState<
                    Resource<YimData>
                    >
            = mutableStateOf(Resource.loading())
    
    /** User name.
        Don't worry about this being nullable as we are performing login check.*/
    private var username: String? = LoginSharedPreferences.username
    
    // Network Checking variables
    private val connectivityObserver = NetworkConnectivityObserver(context)
    private var networkStatus:
            MutableState<ConnectivityObserver.NetworkStatus>
    = mutableStateOf(ConnectivityObserver.NetworkStatus.Unavailable)   // initial value
    
    init {
        checkNetworkStatus()
        getData()
    }
    
    private fun getData() {
        viewModelScope.launch {
            resourceState.value = username?.let { repository.getYimData(username = it) }!!
        }
    }
    
    fun getYimData(): Resource<YimData>{
        return resourceState.value
    }
    
    
    private fun getUserName() : String?{
        return username
    }
    fun isLoggedIn() : Boolean{
        return (LoginSharedPreferences.loginStatus == LoginSharedPreferences.STATUS_LOGGED_IN)
    }
    
    
    private fun checkNetworkStatus(){
        connectivityObserver.observe().onEach {
            networkStatus.value = it
        }.launchIn(viewModelScope)
    }
    fun getNetworkStatus() : ConnectivityObserver.NetworkStatus{
        return networkStatus.value
    }
    
    // TODO: Add functions to get field data directly from viewModel
}