package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.ResponseError
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val appPreferences: AppPreferences,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val inputQueryFlow = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(750).distinctUntilChanged()
    private val resultFlow = MutableStateFlow<List<User>>(emptyList())
    private val errorFlow = MutableStateFlow<ResponseError?>(null)
    
    val uiState = createUiStateFlow()
    
    
    init {
        // Engage query flow
        viewModelScope.launch(ioDispatcher) {
            queryFlow.collectLatest { username ->
                if (username.isEmpty()) return@collectLatest
                val result = repository.searchUser(username)
                when (result.status) {
                    Resource.Status.SUCCESS -> resultFlow.emit(result.data?.users ?: emptyList())
                    Resource.Status.FAILED -> emitError(result.error)
                    Resource.Status.LOADING -> return@collectLatest
                }
            }
        }
    }
    
    
    private fun createUiStateFlow(): StateFlow<SearchUiState> {
        return combine(
            inputQueryFlow,
            resultFlow,
            errorFlow
        ){ query: String, users: List<User>, error: ResponseError? ->
            return@combine SearchUiState(query, users, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchUiState("", emptyList(), null)
        )
    }
    
    
    fun updateQueryFlow(query: String) {
        viewModelScope.launch {
            inputQueryFlow.emit(query)
        }
    }
    
    
    suspend fun toggleFollowStatus(user: User): Flow<Boolean> = flow {
        if (user.username.isEmpty()) {
            emit(false)
            return@flow
        }
        
        val isSuccessful = if (user.isFollowed)
            optimisticallyUnfollowUser(user)
        else
            optimisticallyFollowUser(user)
        
        emit(isSuccessful)
        
    }
    
    
    private suspend fun optimisticallyFollowUser(user: User): Boolean {
        // Updating the list's follow button for the given user beforehand.
        invertFollowStatus(user)
        
        val result = repository.followUser(user.username, appPreferences.lbAccessToken ?: "")
        return when (result.status) {
            Resource.Status.FAILED -> {
                // Revert back ui state of follow button in case something goes wrong.
                invertFollowStatus(user)
                emitError(result.error)
                false
            }
            else -> true
        }
    }
    
    
    private suspend fun optimisticallyUnfollowUser(user: User): Boolean {
        // Updating the list's follow button for the given user beforehand.
        invertFollowStatus(user)
        
        val result = repository.unfollowUser(user.username, appPreferences.lbAccessToken ?: "")
        return when (result.status) {
            Resource.Status.FAILED -> {
                // Revert back ui state of follow button in case something goes wrong.
                invertFollowStatus(user)
                emitError(result.error)
                false
            }
            else -> true
        }
    }
    
    /** Inverts the follow button's state for a particular user. Runs on [DefaultDispatcher].*/
    private suspend fun invertFollowStatus(user: User) = withContext(defaultDispatcher) {
        resultFlow.update {
            /* Since we know the result set is limited to 10, calculation is easy.*/
            val list = resultFlow.value
            val index = list.indexOf(user)
            if (index == -1) return@withContext     // User may have commenced the search of another query by now.
    
            // Inverting state
            list[index].isFollowed = !list[index].isFollowed
            return@update list
        }
    }
    
    private suspend fun emitError(error: ResponseError?){ errorFlow.emit(error) }
    
    /** Call this function to reset [errorFlow]'s latest emission.*/
    fun clearErrorFlow() {
        viewModelScope.launch {
            errorFlow.emit(null)
        }
    }
    
}