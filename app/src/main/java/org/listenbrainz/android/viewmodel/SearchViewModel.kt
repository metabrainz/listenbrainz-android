package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.SearchResult
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: SocialRepository) : ViewModel() {
    
    private val _searchState = MutableStateFlow<Resource<SearchResult>>(Resource.loading())
    val searchState = _searchState.asStateFlow()
    
    fun searchUser(username: String) {
        viewModelScope.launch {
            _searchState.update { Resource.loading() }
            val result = repository.searchUser(username)
            _searchState.value = result
        }
    }
    
    
    
}