package org.listenbrainz.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.UserListUiState
import org.listenbrainz.android.repository.AppPreferences
import org.listenbrainz.android.repository.SocialRepository
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val appPreferences: AppPreferences,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val inputQueryFlow = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    private val errorFlow = MutableStateFlow<ResponseError?>(null)
    
    // Result flows
    private val userListFlow = MutableStateFlow<List<User>>(emptyList())
    private val followStateFlow = MutableStateFlow<List<Boolean>>(emptyList())
    private val resultFlow = userListFlow
        .combineTransform(followStateFlow) { userList, isFollowedList ->
            emit(UserListUiState(userList, isFollowedList))
        }
    
    val uiState = createUiStateFlow()
    
    init {
        // Engage query flow
        viewModelScope.launch(ioDispatcher) {
            queryFlow.collectLatest { username ->
                if (username.isEmpty()){
                    userListFlow.emit(emptyList())
                    return@collectLatest
                }
                
                val result = repository.searchUser(username)
                when (result.status) {
                    Resource.Status.SUCCESS -> userListFlow.emit(result.data?.users ?: emptyList())
                    Resource.Status.FAILED -> emitError(result.error)
                    else -> return@collectLatest
                }
            }
        }
        
        // Observing changes in userListFlow
        viewModelScope.launch(defaultDispatcher) {
            userListFlow.collectLatest { userList ->
                if (userList.isEmpty()) {
                    followStateFlow.emit(emptyList())
                    return@collectLatest
                }
                
                val followList = mutableListOf<Boolean>()
                userList.forEach {
                    followList.add(it.isFollowed)
                }
                
                followStateFlow.emit(followList)
                
            }
        }
        
    }
    
    
    private fun createUiStateFlow(): StateFlow<SearchUiState> {
        return combine(
            inputQueryFlow,
            resultFlow,
            errorFlow
        ){ query: String, users: UserListUiState, error: ResponseError? ->
            return@combine SearchUiState(query, users, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchUiState("", UserListUiState(), null)
        )
    }
    
    
    fun updateQueryFlow(query: String) {
        viewModelScope.launch {
            inputQueryFlow.emit(query)
        }
    }
    
    
    suspend fun toggleFollowStatus(user: User, index: Int) {
        
        if (user.username.isEmpty()) return
        
        if (followStateFlow.value[index])
            optimisticallyUnfollowUser(user, index)
        else
            optimisticallyFollowUser(user, index)
        
    }
    
    
    private suspend fun optimisticallyFollowUser(user: User, index: Int) {
        
        invertFollowUiState(index)
        
        val result = repository.followUser(user.username, appPreferences.lbAccessToken ?: "")
        return when (result.status) {
            Resource.Status.FAILED -> {
                emitError(result.error)
                invertFollowUiState(index)
            }
            else -> Unit
        }
    }
    
    
    private suspend fun optimisticallyUnfollowUser(user: User, index: Int) {
        
        invertFollowUiState(index)
        
        val result = repository.unfollowUser(user.username, appPreferences.lbAccessToken ?: "")
        return when (result.status) {
            Resource.Status.FAILED -> {
                invertFollowUiState(index)
                emitError(result.error)
            }
            else -> Unit
        }
    }
    
    private fun invertFollowUiState(index: Int) {
        
        followStateFlow.getAndUpdate { list ->
            val mutableList = list.toMutableList()
            try {
                mutableList[index] = !mutableList[index]
            } catch (e: IndexOutOfBoundsException){
                // This means query has already changed while we were evaluating this function.
                return@getAndUpdate list
            }
            return@getAndUpdate mutableList
        }
        
    }
    
    private suspend fun emitError(error: ResponseError?){ errorFlow.emit(error) }
    
    /** Call this function to reset [errorFlow]'s latest emission.*/
    fun clearErrorFlow() {
        viewModelScope.launch {
            errorFlow.emit(null)
        }
    }
    
    fun clearUi() {
        viewModelScope.launch {
            userListFlow.emit(emptyList())
            inputQueryFlow.emit("")
        }
    }
}