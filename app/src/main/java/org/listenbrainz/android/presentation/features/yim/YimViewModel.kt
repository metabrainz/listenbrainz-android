package org.listenbrainz.android.presentation.features.yim

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.repository.YimRepositoryImpl
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.presentation.features.login.LoginSharedPreferences
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class YimViewModel @Inject constructor(private val repository: YimRepository) : ViewModel() {
    private var resourceState:
            MutableState<
                    Resource<YimData>
                    >
            = mutableStateOf(Resource.loading())
    
    private var username: String? = null
    
    init {
        getUserName()
        getData()
    }
    
    private fun getData() {
        viewModelScope.launch {
            resourceState.value = username?.let { repository.getYimData(username = it) }!!
        }
    }
    
    // TODO: Perform error check here
    fun getYimData(): Resource<YimData>{
        return resourceState.value
    }
    
    private fun getUserName(){
        username = LoginSharedPreferences.username
    }
    
    fun isLoggedIn() : Boolean{
        return (LoginSharedPreferences.loginStatus == LoginSharedPreferences.STATUS_LOGGED_IN)
    }
    
    // TODO: Add functions to get field data directly from viewModel
}