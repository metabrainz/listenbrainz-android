package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SocialRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FollowUnfollowModel<SearchUiState>(repository, ioDispatcher) {
    
    private val inputQueryFlow = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    
    // Result flows
    private val userListFlow = MutableStateFlow<List<User>>(emptyList())
    private val followStateFlow = MutableStateFlow<List<Boolean>>(emptyList())
    private val resultFlow = userListFlow
        .combineTransform(followStateFlow) { userList, isFollowedList ->
            emit(UserListUiState(userList, isFollowedList))
        }
    
    override val uiState: StateFlow<SearchUiState> = createUiStateFlow()
    
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
                followStateFlow.emit(userList.map { it.isFollowed })
            }
        }
        
    }
    
    fun toggleFollowStatus(user: User, index: Int) {
        viewModelScope.launch(defaultDispatcher) {
            
            if (user.username.isEmpty()) return@launch
            
            try {
                if (followStateFlow.value[index])
                    optimisticallyUnfollowUser(user, index) { invertFollowUiState(it) }
                else
                    optimisticallyFollowUser(user, index) { invertFollowUiState(it) }
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
        }
    }
    
    override fun createUiStateFlow(): StateFlow<SearchUiState> {
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
    
    
    private fun invertFollowUiState(index: Int) {
        // If the view-model is destroyed at this point, Ui state will not be inverted.
        viewModelScope.launch {
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
    }
    
    
    fun clearUi() {
        viewModelScope.launch {
            userListFlow.emit(emptyList())
            inputQueryFlow.emit("")
        }
    }
}