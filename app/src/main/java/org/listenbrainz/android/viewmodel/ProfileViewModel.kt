package org.listenbrainz.android.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.socket.SocketRepository
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.ui.screens.profile.ListensTabUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    val userRepository: UserRepository,
    val listensRepository: ListensRepository,
    val socketRepository: SocketRepository,
    val socialRepository: SocialRepository,
    private val savedStateHandle: SavedStateHandle,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : BaseViewModel<ProfileUiState>() {
    
    private val _loginStatusFlow: MutableStateFlow<Int> = MutableStateFlow(STATUS_LOGGED_OUT)
    val loginStatusFlow: StateFlow<Int> = _loginStatusFlow.asStateFlow()
    private val listenStateFlow : MutableStateFlow<ListensTabUiState> = MutableStateFlow(ListensTabUiState())
    private var username : String? = savedStateHandle.get<String>("username")
    init {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.getLoginStatusFlow()
                .stateIn(this)
                .collectLatest {
                    _loginStatusFlow.emit(it)
                }
        }
        viewModelScope.launch {
            getUserListensData()
        }
    }


    private suspend fun getUserListensData() {
        if(username == null){
            username = appPreferences.username.get()
        }
        val listenCount = userRepository.fetchUserListenCount(username).data?.payload?.count
        val followers = socialRepository.getFollowers(username).data?.followers
        val followersCount = followers?.size

        val listensTabState = ListensTabUiState(
            listenCount = listenCount,
            followersCount = followersCount,
            followers = followers
        )
        listenStateFlow.emit(listensTabState)

    }

    override val uiState: StateFlow<ProfileUiState> = createUiStateFlow()


    override fun createUiStateFlow(): StateFlow<ProfileUiState> {
        return combine(
            listenStateFlow
        ) {
            array ->
            ProfileUiState(array[0])
        }.stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            ProfileUiState()
        )
    }
}