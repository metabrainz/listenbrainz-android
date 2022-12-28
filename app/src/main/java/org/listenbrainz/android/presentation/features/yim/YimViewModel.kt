package org.listenbrainz.android.presentation.features.yim

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.listenbrainz.android.data.repository.YimRepository
import org.listenbrainz.android.data.sources.api.entities.yimdata.YimData
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class YimViewModel @Inject constructor(private val repository: YimRepository) : ViewModel() {
    private var resourceState:
            MutableState<
                    Resource<YimData>
                    >
            = mutableStateOf(Resource.loading())
    
    // TODO: Get username
    private var username = "jasjeet"
    
    init {
        getData()
    }
    
    private fun getData() {
        viewModelScope.launch {
            resourceState.value = repository.getYimData(username = username)
        }
    }
    
    // TODO: Perform error check here
    fun getYimData(): Resource<YimData>{
        return resourceState.value
    }
    
    // TODO: Add functions to get field data directly from viewModel
}