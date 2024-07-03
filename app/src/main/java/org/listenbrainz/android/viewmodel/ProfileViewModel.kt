package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.ui.screens.profile.ListensTabUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.StatsTabUIState
import org.listenbrainz.android.ui.screens.profile.TasteTabUIState
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val userRepository: UserRepository,
    private val listensRepository: ListensRepository,
    private val socialRepository: SocialRepository,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,

) : BaseViewModel<ProfileUiState>() {
    
    private val _loginStatusFlow: MutableStateFlow<Int> = MutableStateFlow(STATUS_LOGGED_OUT)
    private var isLoggedInUser = false
    val loginStatusFlow: StateFlow<Int> = _loginStatusFlow.asStateFlow()
    private val listenStateFlow : MutableStateFlow<ListensTabUiState> = MutableStateFlow(ListensTabUiState())
    private val statsStateFlow : MutableStateFlow<StatsTabUIState> = MutableStateFlow(StatsTabUIState())
    private val tasteStateFlow : MutableStateFlow<TasteTabUIState> = MutableStateFlow(TasteTabUIState())

    init {
        viewModelScope.launch(ioDispatcher) {
            appPreferences.getLoginStatusFlow()
                .stateIn(this)
                .collectLatest {
                    _loginStatusFlow.emit(it)
                }
        }
    }

    private suspend fun getSimilarArtists(username: String?) : List<String> {
        val currentUsername = appPreferences.username.get()
        val currentUserTopArtists = userRepository.getTopArtists(currentUsername)
        val userTopArtists = userRepository.getTopArtists(username)
        val similarArtists = mutableListOf<String>()
        currentUserTopArtists.data?.payload?.artists?.map {
            currentUserTopArtist ->
            userTopArtists.data?.payload?.artists?.map{
                userTopArtist ->
                if(currentUserTopArtist.artist_name == userTopArtist.artist_name){
                    similarArtists.add(currentUserTopArtist.artist_name)
                }
            }
        }
        return similarArtists.distinct()
    }

    fun followUser(username: String?){
        if(username.isNullOrEmpty()) return
        viewModelScope.launch (ioDispatcher) {
            socialRepository.followUser(username)
        }
    }

    fun unfollowUser(username: String?){
        if(username.isNullOrEmpty()) return
        viewModelScope.launch(ioDispatcher) {
            socialRepository.unfollowUser(username)
        }

    }

    suspend fun getUserDataFromRemote(
        inputUsername: String?
    ) = coroutineScope{
        val listensTabData = async{ getUserListensData(inputUsername) }
        val statsTabData = async {getUserStatsData(inputUsername)}
        val tasteTabData = async {getUserTasteData(inputUsername)}
        listensTabData.await()
        statsTabData.await()
        tasteTabData.await()
    }



    private suspend fun getUserListensData(inputUsername: String?) {
        val username = inputUsername ?: appPreferences.username.get()
        if(inputUsername != null){
            isLoggedInUser = inputUsername == appPreferences.username.get()
        }
        val listenCount = userRepository.fetchUserListenCount(username).data?.payload?.count
        val listens: List<Listen>? = listensRepository.fetchUserListens(username).data?.payload?.listens
        val followers = socialRepository.getFollowers(username).data?.followers
        val currentUserFollowing = socialRepository.getFollowing(appPreferences.username.get()).data?.following
        val followersState : MutableList<Pair<String,Boolean>> = mutableListOf()
        val followingState : MutableList<Pair<String,Boolean>> = mutableListOf()
        val followersCount = followers?.size
        val following = socialRepository.getFollowing(username).data?.following
        val currentUserFollowingSet = currentUserFollowing?.toSet() ?: emptySet()
        viewModelScope.launch {
            followers?.forEach { user ->
                val isFollowing = currentUserFollowingSet.contains(user)
                followersState.add(Pair(user, isFollowing))
            }
            following?.forEach { user ->
                val isFollowing = currentUserFollowingSet.contains(user)
                followingState.add(Pair(user, isFollowing))
            }
        }
        val followingCount = following?.size
        val similarUsers = socialRepository.getSimilarUsers(username).data?.payload
        val currentPins = userRepository.fetchUserCurrentPins(username).data
        val compatibility = if (username != appPreferences.username.get())
            userRepository.fetchUserSimilarity(
                appPreferences.username.get(),
                username
            ).data?.userSimilarity?.similarity
        else 0f
        val similarArtists = getSimilarArtists(username)
        val isFollowing = currentUserFollowingSet.contains(username)
        val listensTabState = ListensTabUiState(
            isLoading = false,
            listenCount = listenCount,
            followersCount = followersCount,
            followers = followersState,
            followingCount = followingCount,
            following = followingState,
            recentListens = listens,
            compatibility = compatibility,
            similarUsers = similarUsers,
            pinnedSong = currentPins,
            similarArtists = similarArtists,
            isFollowing = isFollowing
        )
        listenStateFlow.emit(listensTabState)
    }

    private suspend fun getUserStatsData(inputUsername: String?) {

    }

    private suspend fun getUserTasteData(inputUsername: String?) {
        val lovedSongs = userRepository.getUserFeedback(inputUsername, 1).data
        val hatedSongs = userRepository.getUserFeedback(inputUsername, -1).data
        val tastesTabState = TasteTabUIState(
            isLoading = false,
            lovedSongs = lovedSongs,
            hatedSongs = hatedSongs,
        )
        tasteStateFlow.emit(tastesTabState)
    }


    override val uiState: StateFlow<ProfileUiState> = createUiStateFlow()


    override fun createUiStateFlow(): StateFlow<ProfileUiState> {
        return combine(
            listenStateFlow,
            statsStateFlow,
            tasteStateFlow,
        ) {
            listensUIState, statsUIState, tasteUIState ->
            ProfileUiState(
                isSelf = isLoggedInUser,
                listensTabUiState = listensUIState,
                statsTabUIState = statsUIState,
                tasteTabUIState = tasteUIState,
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            ProfileUiState()
        )
    }
}