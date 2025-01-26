package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.repository.user.UserRepository
import org.listenbrainz.android.ui.screens.profile.CreatedForTabUIState
import org.listenbrainz.android.ui.screens.profile.ListensTabUiState
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.StatsTabUIState
import org.listenbrainz.android.ui.screens.profile.TasteTabUIState
import org.listenbrainz.android.ui.screens.profile.stats.StatsRange
import org.listenbrainz.android.ui.screens.profile.stats.UserGlobal
import org.listenbrainz.android.util.Constants.Strings.STATUS_LOGGED_OUT
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val userRepository: UserRepository,
    private val listensRepository: ListensRepository,
    private val socialRepository: SocialRepository,
    private val playlistDataRepository: PlaylistDataRepository,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel<ProfileUiState>() {

    private var isLoggedInUser = false
    val loginStatusFlow: StateFlow<Int> =
        appPreferences
            .getLoginStatusFlow()
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                STATUS_LOGGED_OUT
            )
    private val listenStateFlow : MutableStateFlow<ListensTabUiState> = MutableStateFlow(ListensTabUiState())
    private val statsStateFlow : MutableStateFlow<StatsTabUIState> = MutableStateFlow(StatsTabUIState())
    private val tasteStateFlow : MutableStateFlow<TasteTabUIState> = MutableStateFlow(TasteTabUIState())
    private val createdForFlow: MutableStateFlow<CreatedForTabUIState> = MutableStateFlow(CreatedForTabUIState())

    private suspend fun getSimilarArtists(username: String?) : List<org.listenbrainz.android.model.user.Artist> {
        val currentUsername = appPreferences.username.get()
        val currentUserTopArtists = userRepository.getTopArtists(currentUsername, count = 100)
        val userTopArtists = userRepository.getTopArtists(username, count = 100)
        val similarArtists = mutableListOf<org.listenbrainz.android.model.user.Artist>()
        currentUserTopArtists.data?.payload?.artists?.map {
            currentUserTopArtist ->
            userTopArtists.data?.payload?.artists?.map{
                userTopArtist ->
                if(currentUserTopArtist.artistName == userTopArtist.artistName){
                    similarArtists.add(currentUserTopArtist)
                }
            }
        }
        return similarArtists.distinct()
    }

    fun followUser(username: String?) {
        if (username.isNullOrEmpty()) return
        updateFollowState(username, true)

        viewModelScope.launch(ioDispatcher) {
            val result = socialRepository.followUser(username)
            if (result.status == Resource.Status.FAILED) {
                updateFollowState(username, false)
            }
        }
    }

    fun unfollowUser(username: String?) {
        if (username.isNullOrEmpty()) return
        updateFollowState(username, false)

        viewModelScope.launch(ioDispatcher) {
            val result = socialRepository.unfollowUser(username)
            if (result.status == Resource.Status.FAILED) {
                updateFollowState(username, true)
            }
        }
    }

    private fun updateFollowState(username: String, isFollowing: Boolean) {
        val updatedFollowers = listenStateFlow.value.followers?.map { (user, status) ->
            if (user == username) user to isFollowing else user to status
        }
        val updatedFollowing = listenStateFlow.value.following?.map { (user, status) ->
            if (user == username) user to isFollowing else user to status
        }
        listenStateFlow.value = listenStateFlow.value.copy(
            followers = updatedFollowers,
            following = updatedFollowing,
            isFollowing = isFollowing
        )
    }

    suspend fun getUserDataFromRemote(
        inputUsername: String?
    ) = coroutineScope{
        val listensTabData = async { getUserListensData(inputUsername) }
        val statsTabData = async {getUserStatsData(inputUsername)}
        val tasteTabData = async {getUserTasteData(inputUsername)}
        val createdForTabData = async {getCreatedForYouPlaylists(inputUsername)}
        listensTabData.await()
        statsTabData.await()
        tasteTabData.await()
        createdForTabData.await()
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
        val currentPins = userRepository.fetchUserCurrentPins(username).data?.pinnedRecording
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
        val userThisWeekListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.THIS_WEEK.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userThisMonthListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.THIS_MONTH.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userThisYearListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.THIS_YEAR.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userLastWeekListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.LAST_WEEK.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userLastMonthListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.LAST_MONTH.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userLastYearListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.LAST_YEAR.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val userAllTimeListeningActivity = userRepository.getUserListeningActivity(inputUsername, StatsRange.ALL_TIME.apiIdenfier).data?.payload?.listeningActivity ?: listOf()

        val globalThisWeekListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.THIS_WEEK.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalThisMonthListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.THIS_MONTH.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalThisYearListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.THIS_YEAR.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalLastWeekListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.LAST_WEEK.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalLastMonthListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.LAST_MONTH.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalLastYearListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.LAST_YEAR.apiIdenfier).data?.payload?.listeningActivity ?: listOf()
        val globalAllTimeListeningActivity = userRepository.getGlobalListeningActivity(StatsRange.ALL_TIME.apiIdenfier).data?.payload?.listeningActivity ?: listOf()

        val userListeningActivityMap = mapOf(
            Pair(UserGlobal.USER, StatsRange.THIS_WEEK)  to userThisWeekListeningActivity,
            Pair(UserGlobal.USER, StatsRange.THIS_MONTH) to userThisMonthListeningActivity,
            Pair(UserGlobal.USER, StatsRange.THIS_YEAR)  to userThisYearListeningActivity,
            Pair(UserGlobal.USER, StatsRange.LAST_WEEK)  to userLastWeekListeningActivity,
            Pair(UserGlobal.USER, StatsRange.LAST_MONTH) to userLastMonthListeningActivity,
            Pair(UserGlobal.USER, StatsRange.LAST_YEAR)  to userLastYearListeningActivity,
            Pair(UserGlobal.USER, StatsRange.ALL_TIME)   to userAllTimeListeningActivity,

            Pair(UserGlobal.GLOBAL, StatsRange.THIS_WEEK)  to globalThisWeekListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.THIS_MONTH) to globalThisMonthListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.THIS_YEAR)  to globalThisYearListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.LAST_WEEK)  to globalLastWeekListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.LAST_MONTH) to globalLastMonthListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.LAST_YEAR)  to globalLastYearListeningActivity,
            Pair(UserGlobal.GLOBAL, StatsRange.ALL_TIME)   to globalAllTimeListeningActivity,
        )

        val statsTabState = StatsTabUIState(
            isLoading = false,
            userListeningActivity = userListeningActivityMap,
        )

        statsStateFlow.emit(statsTabState)
    }

    suspend fun getUserTopArtists(inputUsername: String?){
        statsStateFlow.value = statsStateFlow.value.copy(isLoading = true)
        val userTopArtistsThisWeek = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.THIS_WEEK.apiIdenfier).data
        val userTopArtistsThisMonth = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.THIS_MONTH.apiIdenfier).data
        val userTopArtistsThisYear = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.THIS_YEAR.apiIdenfier).data
        val userTopArtistsLastWeek = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.LAST_WEEK.apiIdenfier).data
        val userTopArtistsLastMonth = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.LAST_MONTH.apiIdenfier).data
        val userTopArtistsLastYear = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.LAST_YEAR.apiIdenfier).data
        val userTopArtistsAllTime = userRepository.getTopArtists(inputUsername, rangeString = StatsRange.ALL_TIME.apiIdenfier).data

        val topArtists = mapOf(
            StatsRange.THIS_WEEK to userTopArtistsThisWeek,
            StatsRange.THIS_MONTH to userTopArtistsThisMonth,
            StatsRange.THIS_YEAR to userTopArtistsThisYear,
            StatsRange.LAST_WEEK to userTopArtistsLastWeek,
            StatsRange.LAST_MONTH to userTopArtistsLastMonth,
            StatsRange.LAST_YEAR to userTopArtistsLastYear,
            StatsRange.ALL_TIME to userTopArtistsAllTime
        )

        statsStateFlow.value = statsStateFlow.value.copy(
            isLoading = false,
            topArtists = topArtists
        )
    }


    suspend fun getUserTopAlbums(inputUsername: String?){
        statsStateFlow.value = statsStateFlow.value.copy(isLoading = true)
        val userTopAlbumsThisWeek = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.THIS_WEEK.apiIdenfier).data
        val userTopAlbumsThisMonth = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.THIS_MONTH.apiIdenfier).data
        val userTopAlbumsThisYear = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.THIS_YEAR.apiIdenfier).data
        val userTopAlbumsLastWeek = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.LAST_WEEK.apiIdenfier).data
        val userTopAlbumsLastMonth = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.LAST_MONTH.apiIdenfier).data
        val userTopAlbumsLastYear = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.LAST_YEAR.apiIdenfier).data
        val userTopAlbumsAllTime = userRepository.getTopAlbums(inputUsername, rangeString = StatsRange.ALL_TIME.apiIdenfier).data

        val topAlbums = mapOf(
            StatsRange.THIS_WEEK to userTopAlbumsThisWeek,
            StatsRange.THIS_MONTH to userTopAlbumsThisMonth,
            StatsRange.THIS_YEAR to userTopAlbumsThisYear,
            StatsRange.LAST_WEEK to userTopAlbumsLastWeek,
            StatsRange.LAST_MONTH to userTopAlbumsLastMonth,
            StatsRange.LAST_YEAR to userTopAlbumsLastYear,
            StatsRange.ALL_TIME to userTopAlbumsAllTime
        )

        statsStateFlow.value = statsStateFlow.value.copy(
            isLoading = false,
            topAlbums = topAlbums
        )
    }

    suspend fun getUserTopSongs(inputUsername: String?){
        statsStateFlow.value = statsStateFlow.value.copy(isLoading = true)
        val userTopSongsThisWeek = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.THIS_WEEK.apiIdenfier).data
        val userTopSongsThisMonth = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.THIS_MONTH.apiIdenfier).data
        val userTopSongsThisYear = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.THIS_YEAR.apiIdenfier).data
        val userTopSongsLastWeek = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.LAST_WEEK.apiIdenfier).data
        val userTopSongsLastMonth = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.LAST_MONTH.apiIdenfier).data
        val userTopSongsLastYear = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.LAST_YEAR.apiIdenfier).data
        val userTopSongsAllTime = userRepository.getTopSongs(inputUsername, rangeString = StatsRange.ALL_TIME.apiIdenfier).data

        val topSongs = mapOf(
            StatsRange.THIS_WEEK to userTopSongsThisWeek,
            StatsRange.THIS_MONTH to userTopSongsThisMonth,
            StatsRange.THIS_YEAR to userTopSongsThisYear,
            StatsRange.LAST_WEEK to userTopSongsLastWeek,
            StatsRange.LAST_MONTH to userTopSongsLastMonth,
            StatsRange.LAST_YEAR to userTopSongsLastYear,
            StatsRange.ALL_TIME to userTopSongsAllTime
        )

        statsStateFlow.value = statsStateFlow.value.copy(
            isLoading = false,
            topSongs = topSongs
        )
    }

    private suspend fun getUserTasteData(inputUsername: String?) {
        val lovedSongs = userRepository.getUserFeedback(inputUsername, 1).data
        val hatedSongs = userRepository.getUserFeedback(inputUsername, -1).data
        val userPins = userRepository.fetchUserPins(inputUsername).data
        val tastesTabState = TasteTabUIState(
            isLoading = false,
            lovedSongs = lovedSongs,
            hatedSongs = hatedSongs,
            pins = userPins,
        )
        tasteStateFlow.emit(tastesTabState)
    }

    //This function gets the createdForYou playlists and fetches the playlist data for each playlist
    private suspend fun getCreatedForYouPlaylists(inputUsername: String?) {
        createdForFlow.value = createdForFlow.value.copy(isLoading = true)
        val createdForYouPlaylists = userRepository.getCreatedForYouPlaylists(inputUsername).data
        //Map to store the playlist data for each playlist
        val createdForYouPlaylistData = mutableMapOf<String, PlaylistData>()
        //Fetch the playlist data for each playlist
        createdForYouPlaylists?.playlists?.forEach { data->
            val playlistMbid = data.getPlaylistMBID()
            if (playlistMbid != null) {
                val playlistData = playlistDataRepository.fetchPlaylist(playlistMbid).data
                if (playlistData != null) {
                    createdForYouPlaylistData[playlistMbid] = playlistData.playlist
                }
            }

        }
        val createdForTabState = CreatedForTabUIState(
            isLoading = false,
            createdForYouPlaylists = createdForYouPlaylists?.playlists,
            createdForYouPlaylistData = createdForYouPlaylistData
        )

        createdForFlow.emit(createdForTabState)
    }

    //This function saves the createdForYou playlist to the user's account
    fun saveCreatedForPlaylist(playlistMbid: String?,
                               onCompletion: (String)->Unit
    ){
        viewModelScope.launch(ioDispatcher) {
            val result = playlistDataRepository.copyPlaylist(playlistMbid)
            if (result.status == Resource.Status.SUCCESS){
                //Show a snackbar with the playlist id
                onCompletion("Playlist saved successfully with id ${result.data?.playlistMbid}")
            }
            else{
                emitError(result.error)
            }
        }
    }

    override val uiState: StateFlow<ProfileUiState> = createUiStateFlow()


    override fun createUiStateFlow(): StateFlow<ProfileUiState> {
        return combine(
            listenStateFlow,
            statsStateFlow,
            tasteStateFlow,
            createdForFlow
        ) {
            listensUIState, statsUIState, tasteUIState, createdForUIState ->
            ProfileUiState(
                isSelf = isLoggedInUser,
                listensTabUiState = listensUIState,
                statsTabUIState = statsUIState,
                tasteTabUIState = tasteUIState,
                createdForTabUIState = createdForUIState
            )
        }.stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            ProfileUiState()
        )
    }
}