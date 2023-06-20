package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.ResponseError
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: SocialRepository) : ViewModel() {
    
    private val inputQueryFlow = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(500)
    
    private val resultFlow = MutableStateFlow<List<User>>(emptyList())
    private val errorFlow = MutableStateFlow<ResponseError?>(null)
    
    val uiState = createUiStateFlow()
    
    init {
        viewModelScope.launch(Dispatchers.IO) {
            queryFlow.collectLatest { username ->
                val result = repository.searchUser(username)
                when (result.status) {
                    Resource.Status.SUCCESS -> {
                        resultFlow.emit(result.data?.users ?: emptyList())
                        errorFlow.emit(null)
                    }
                    Resource.Status.FAILED -> {
                        errorFlow.emit(result.error)
                    }
                    else -> {}
                }
                errorFlow.value?.toast()
            }
        }
    }
    
    private fun createUiStateFlow(): StateFlow<SearchUiState> {
        return inputQueryFlow.asStateFlow().combine(resultFlow){ s: String, users: List<User> ->
            return@combine SearchUiState(users, s)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchUiState(emptyList(), "")
        )
    }
    
    fun updateQueryFlow(query: String) {
        viewModelScope.launch {
            inputQueryFlow.emit(query)
        }
    }
    
}