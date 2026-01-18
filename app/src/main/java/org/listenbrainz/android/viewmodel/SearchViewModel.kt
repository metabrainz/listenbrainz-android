package org.listenbrainz.android.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.search.artistSearch.ArtistSearchUiState
import org.listenbrainz.android.model.search.artistSearch.ArtistUiModel
import org.listenbrainz.android.model.search.playlistSearch.PlayListSearchUiState
import org.listenbrainz.android.model.search.playlistSearch.PlaylistUiModel
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.search.SearchData
import org.listenbrainz.android.model.search.SearchType
import org.listenbrainz.android.model.search.SearchUiState
import org.listenbrainz.android.model.search.trackSearch.TrackSearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.search.userSearch.UserListUiState
import org.listenbrainz.android.model.albumSearch.toUiModel
import org.listenbrainz.android.model.artistSearch.toUiModel
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.playlist.toUiModel
import org.listenbrainz.android.model.search.albumSearch.AlbumSearchUiState
import org.listenbrainz.android.model.search.albumSearch.AlbumUiModel
import org.listenbrainz.android.repository.album.AlbumRepository
import org.listenbrainz.android.repository.artist.ArtistRepository
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Log
import org.listenbrainz.android.util.Resource


class SearchViewModel(
    private val userRepository: SocialRepository,
    private val playlistRepository: PlaylistDataRepository,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher,
) : FollowUnfollowModel<SearchUiState>(userRepository, ioDispatcher) {
    private val inputQueryFlow = MutableStateFlow("")
    
    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    
    // Result flows
    private val userListFlow = MutableStateFlow<List<User>>(emptyList())
    private val playlistFlow = MutableStateFlow<List<PlaylistUiModel>>(emptyList())

    private val artistFlow = MutableStateFlow<List<ArtistUiModel>>(emptyList())

    private val albumFlow = MutableStateFlow<List<AlbumUiModel>>(emptyList())
    private val trackFlow = MutableStateFlow<List<PlaylistTrack>>(emptyList())

    private val followStateFlow = MutableStateFlow<List<Boolean>>(emptyList())

    val isLoadingFlow = MutableStateFlow(false)

    private val combinedUserFlow = combine(userListFlow, followStateFlow) { users, followList ->
        UserListUiState(users, followList)
    }
    private val resultFlow: StateFlow<SearchData?> = combine(
        combinedUserFlow,
        playlistFlow,
        artistFlow,
        albumFlow,
        trackFlow
    ) { userState, playlists, artists, albums, tracks ->
        when {
            userState.userList.isNotEmpty() -> SearchData.Users(userState)
            playlists.isNotEmpty() -> SearchData.Playlists(PlayListSearchUiState(playlists))
            artists.isNotEmpty() -> SearchData.Artists(ArtistSearchUiState(artists))
            albums.isNotEmpty() -> SearchData.Albums(AlbumSearchUiState(albums))
            tracks.isNotEmpty() -> SearchData.Tracks(TrackSearchUiState(tracks))
            else -> null
        }
    }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    override val uiState: StateFlow<SearchUiState> by lazy {
        createUiStateFlow()
    }
    val searchOption = MutableStateFlow(SearchType.USER)

    init {
        // Engage query flow
        viewModelScope.launch(ioDispatcher) {
            combine(queryFlow, searchOption) { query, type ->
                query to type
            }.collectLatest { (query, type) ->

                if (query.isEmpty()) {
                    clearUi()
                    return@collectLatest
                }
                resetUi()
                when (type) {
                    SearchType.USER -> {
                        searchUsers(query)
                    }
                    SearchType.PLAYLIST -> {
                        searchPlaylists(query)
                    }
                    SearchType.ARTIST -> {
                        searchArtists(query)
                    }
                    SearchType.TRACK -> {
                        searchTracks(query)
                    }
                    SearchType.ALBUM -> {
                        searchAlbum(query)
                    }
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

    private suspend fun searchUsers(query: String) {
        isLoadingFlow.emit(true)
        try {
            val result = userRepository.searchUser(query)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    val users = result.data?.users ?: emptyList()
                    if (users.isEmpty()) {
                        emitError(ResponseError.Unknown(searchOption.value.resultMessage))
                        userListFlow.emit(emptyList())
                        return
                    }
                    userListFlow.emit(users)
                }

                Resource.Status.FAILED ->
                    emitError(result.error)

                else -> Unit
            }
        }
        finally {
            isLoadingFlow.emit(false)
        }
    }

    private suspend fun searchPlaylists(query: String) {
        isLoadingFlow.emit(true)
        try {
            val result = playlistRepository.searchPlaylists(query)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    val playlists = result.data?.playlists ?: emptyList()
                    val mappedPlaylists = playlists.map { it.playlist.toUiModel() }
                    if (mappedPlaylists.isEmpty()) {
                        emitError(ResponseError.Unknown(searchOption.value.resultMessage))
                        playlistFlow.emit(emptyList())
                        return
                    }
                    playlistFlow.emit(mappedPlaylists)
                }

                Resource.Status.FAILED ->
                    emitError(result.error)

                else -> Unit
            }
        }
        finally {
            isLoadingFlow.emit(false)
        }
    }

    private suspend fun searchArtists(query: String) {
        isLoadingFlow.emit(true)
        try {
            val result = artistRepository.searchArtist(query)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    val artists = result.data?.artists ?: emptyList()
                    val mappedArtists = artists.map { it.toUiModel() }
                    if (mappedArtists.isEmpty()) {
                        emitError(ResponseError.Unknown(searchOption.value.resultMessage))
                        artistFlow.emit(emptyList())
                        return
                    }
                    artistFlow.emit(mappedArtists)
                }

                Resource.Status.FAILED -> {
                    emitError(result.error)
                }
                else -> Unit
            }
        }
        finally {
            isLoadingFlow.emit(false)
        }
    }

    private suspend fun searchAlbum(query: String) {
        isLoadingFlow.emit(true)
        try {
            val result = albumRepository.searchAlbums(query)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    val albums = result.data?.releaseGroups ?: emptyList()
                    val mappedAlbums = albums.map { it.toUiModel() }
                    if (mappedAlbums.isEmpty()) {
                        emitError(ResponseError.Unknown(searchOption.value.resultMessage))
                        albumFlow.emit(emptyList())
                        return
                    }
                    albumFlow.emit(mappedAlbums)
                }

                Resource.Status.FAILED -> {
                    emitError(result.error)
                }


                else -> Unit
            }
        }
        finally {
            isLoadingFlow.emit(false)
        }
    }

    private suspend fun searchTracks(query: String) {
        isLoadingFlow.emit(true)
        try {
            val result = playlistRepository.searchRecording(query)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    val tracks = result.data?.recordings ?: emptyList()
                    val mappedTracks = tracks.map { it.toPlaylistTrack() }
                    if (mappedTracks.isEmpty()) {
                        emitError(ResponseError.Unknown(searchOption.value.resultMessage))
                        trackFlow.emit(emptyList())
                        return
                    }

                    trackFlow.emit(mappedTracks)
                }

                Resource.Status.FAILED -> {
                    emitError(result.error)
                }

                else -> Unit
            }

        }
        finally {
            isLoadingFlow.emit(false)
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

    fun playListen(trackMetadata: TrackMetadata) {
        val spotifyId = trackMetadata.additionalInfo?.spotifyId
        if (spotifyId != null){
            Uri.parse(spotifyId).lastPathSegment?.let { trackId ->
                remotePlaybackHandler.playUri(
                    trackId = trackId,
                    onFailure = { playFromYoutubeMusic(trackMetadata) }
                )
            }
        } else {
            playFromYoutubeMusic(trackMetadata)
        }
    }

    private fun playFromYoutubeMusic(trackMetadata: TrackMetadata) {
        viewModelScope.launch {
            remotePlaybackHandler.apply {
                playOnYoutube {
                    withContext(ioDispatcher) {
                        searchYoutubeMusicVideoId(
                            trackMetadata.trackName
                                ?: return@withContext Resource.failure(ResponseError.DoesNotExist()),
                            trackMetadata.artistName.orEmpty()
                        )
                    }
                }
            }
        }
    }

    override fun createUiStateFlow(): StateFlow<SearchUiState> {
        return combine(
            inputQueryFlow,
            resultFlow,
            errorFlow,
            searchOption
        ) { query: String, result: SearchData?, error: ResponseError?, selectedType ->
            return@combine SearchUiState(
                selectedSearchType = selectedType,
                query = query,
                result = result,
                error = error
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SearchUiState(
                selectedSearchType = SearchType.USER,
                query = "",
                result = null,
                error = null
            )
        )
    }


    fun updateQueryFlow(query: String) {
        viewModelScope.launch {
            inputQueryFlow.emit(query)
        }
    }

    fun updateSearchOption(option: SearchType) {
        viewModelScope.launch { searchOption.emit(option) }
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

    fun resetUi() {
        viewModelScope.launch {
            userListFlow.emit(emptyList())
            followStateFlow.emit(emptyList())
            artistFlow.emit(emptyList())
            playlistFlow.emit(emptyList())
            albumFlow.emit(emptyList())
            trackFlow.emit(emptyList())
        }
    }

    fun clearUi() {
        viewModelScope.launch {
            userListFlow.emit(emptyList())
            playlistFlow.emit(emptyList())
            artistFlow.emit(emptyList())
            albumFlow.emit(emptyList())
            trackFlow.emit(emptyList())
            inputQueryFlow.emit("")
        }
    }
}