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
import org.listenbrainz.android.ui.screens.profile.stats.StatsRange
import org.listenbrainz.android.ui.screens.profile.stats.UserGlobal
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
        val currentUserTopArtists = userRepository.getTopArtists(currentUsername, count = 100)
        val userTopArtists = userRepository.getTopArtists(username, count = 100)
        val similarArtists = mutableListOf<String>()
        currentUserTopArtists.data?.payload?.artists?.map {
            currentUserTopArtist ->
            userTopArtists.data?.payload?.artists?.map{
                userTopArtist ->
                if(currentUserTopArtist.artistName == userTopArtist.artistName){
                    similarArtists.add(currentUserTopArtist.artistName)
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

    private fun extractDayAndMonth(timeRange: String): Pair<Int, Int> {
        val monthOrder = mapOf(
            "January" to 1,
            "February" to 2,
            "March" to 3,
            "April" to 4,
            "May" to 5,
            "June" to 6,
            "July" to 7,
            "August" to 8,
            "September" to 9,
            "October" to 10,
            "November" to 11,
            "December" to 12
        )
        val parts = timeRange.split(" ")
        val month = parts[1]
        val day = parts[0].toIntOrNull() ?: 0
        return Pair(day, monthOrder[month] ?: 0)
    }

    private fun extractMonthAndYear(timeRange: String): Pair<Int, Int> {
        val monthOrder = mapOf(
            "January" to 1,
            "February" to 2,
            "March" to 3,
            "April" to 4,
            "May" to 5,
            "June" to 6,
            "July" to 7,
            "August" to 8,
            "September" to 9,
            "October" to 10,
            "November" to 11,
            "December" to 12
        )
        val parts = timeRange.split(" ")
        val month = parts[0]
        val year = parts[1].toIntOrNull() ?: 0
        return Pair(monthOrder[month] ?: 0, year)
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
        val statsTabState = StatsTabUIState(
            isLoading = false,
            userListeningActivity = userListeningActivityMap,
            topArtists = topArtists
        )
        statsStateFlow.emit(statsTabState)
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