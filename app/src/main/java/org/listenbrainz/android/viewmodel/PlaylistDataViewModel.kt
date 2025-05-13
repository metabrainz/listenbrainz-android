package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.playlist.Extension
import org.listenbrainz.android.model.playlist.MoveTrack
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistExtensionData
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.model.playlist.PlaylistTrack
import org.listenbrainz.android.model.recordingSearch.RecordingData
import org.listenbrainz.android.model.userPlaylist.UserPlaylist
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.ui.screens.playlist.CreateEditScreenUIState
import org.listenbrainz.android.ui.screens.playlist.PlaylistDataUIState
import org.listenbrainz.android.ui.screens.playlist.PlaylistDetailUIState
import org.listenbrainz.android.ui.screens.profile.playlists.CollabPlaylistPagingSource
import org.listenbrainz.android.ui.screens.profile.playlists.UserPlaylistPagingSource
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import org.listenbrainz.android.util.Utils.isValidMbidFormat
import javax.inject.Inject

@HiltViewModel
class PlaylistDataViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val repository: PlaylistDataRepository,
    private val socialRepository: SocialRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<PlaylistDataUIState>() {
    private var username: String? = null
    private val userInputQueryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val userQueryFlow =
        userInputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    private val recordingInputQueryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val recordingQueryFlow =
        recordingInputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    private val userListFlow = MutableStateFlow<List<User>>(emptyList())
    private val playlistData = MutableStateFlow<Map<String, PlaylistData>>(emptyMap())

    private val createEditScreenUIStateFlow: MutableStateFlow<CreateEditScreenUIState> =
        MutableStateFlow(
            CreateEditScreenUIState()
        )

    private val playlistScreenUIStateFlow: MutableStateFlow<PlaylistDetailUIState> =
        MutableStateFlow(
            PlaylistDetailUIState()
        )

    init {
        viewModelScope.launch(ioDispatcher) {
            username = appPreferences.username.get()
            recordingQueryFlow.collectLatest { title ->
                if (title.isEmpty()) {
                    playlistScreenUIStateFlow.emit(
                        playlistScreenUIStateFlow.value.copy(
                            queriedRecordings = emptyList()
                        )
                    )
                    return@collectLatest
                }
                playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(isSearching = true))
                val result = if (isValidMbidFormat(title)) repository.searchRecording(null, title)
                else repository.searchRecording(title)
                playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(isSearching = false))
                when (result.status) {
                    Resource.Status.SUCCESS -> playlistScreenUIStateFlow.emit(
                        playlistScreenUIStateFlow.value.copy(
                            isSearching = false,
                            queriedRecordings = result.data?.recordings ?: emptyList()
                        )
                    )

                    Resource.Status.FAILED -> emitError(result.error)
                    else -> return@collectLatest
                }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            userQueryFlow.collectLatest { username ->
                if (username.isEmpty()) {
                    return@collectLatest
                }
                createEditScreenUIStateFlow.emit(createEditScreenUIStateFlow.value.copy(isSearching = true))
                val result = socialRepository.searchUser(username)
                createEditScreenUIStateFlow.emit(createEditScreenUIStateFlow.value.copy(isSearching = false))
                when (result.status) {
                    Resource.Status.SUCCESS -> userListFlow.emit(result.data?.users ?: emptyList())
                    Resource.Status.FAILED -> emitError(result.error)
                    else -> return@collectLatest
                }
            }
        }
    }

    fun getDataInPlaylistScreen(mbid: String?, isRefresh: Boolean = false) {
        var playlist = PlaylistData()
        viewModelScope.launch(ioDispatcher) {
            username = appPreferences.username.get()
            if (isRefresh)
                playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(isRefreshing = true))
            else
                playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(isLoading = true))
            //First check if data is already fetched
            if (playlistData.value.containsKey(mbid) && !isRefresh) {
                playlist = playlistData.value[mbid]!!
                playlistScreenUIStateFlow.emit(
                    playlistScreenUIStateFlow.value.copy(
                        playlistData = playlist,
                        isLoading = false,
                        playlistMBID = mbid,
                        isRefreshing = false,
                        isUserPlaylistOwner = username == playlist.creator || playlist.extension.playlistExtensionData.collaborators.contains(
                            username
                        )
                    )
                )
            } else {
                //Fetch data from API
                getDataOfPlaylist(mbid, onError = {
                    emitError(it)
                    playlistScreenUIStateFlow.emit(
                        playlistScreenUIStateFlow.value.copy(
                            isLoading = false,
                            isRefreshing = false
                        )
                    )
                }, onSuccess = { playlist ->
                    playlistScreenUIStateFlow.emit(
                        playlistScreenUIStateFlow.value.copy(
                            playlistData = playlist,
                            isLoading = false,
                            playlistMBID = mbid,
                            isRefreshing = false,
                            isUserPlaylistOwner = username == playlist.creator || playlist.extension.playlistExtensionData.collaborators.contains(
                                username
                            )
                        )
                    )
                })
            }
        }
    }

    fun getInitialDataInCreatePlaylistScreen(mbid: String?) {
        var playlist = PlaylistData()
        viewModelScope.launch(ioDispatcher) {
            username = appPreferences.username.get()
            if (mbid == null) {
                createEditScreenUIStateFlow.emit(CreateEditScreenUIState(isLoading = false))
                return@launch
            }
            createEditScreenUIStateFlow.emit(createEditScreenUIStateFlow.value.copy(isLoading = true))
            //First check if data is already fetched
            if (playlistData.value.containsKey(mbid)) {
                playlist = playlistData.value[mbid]!!
                createEditScreenUIStateFlow.emit(
                    createEditScreenUIStateFlow.value.copy(
                        playlistData = playlist,
                        isLoading = false,
                        name = playlist.title ?: "",
                        description = Utils.removeHtmlTags(playlist.annotation ?: ""),
                        isPublic = playlist.extension.playlistExtensionData.public ?: false,
                        playlistMBID = mbid,
                        collaboratorSelected = playlist.extension.playlistExtensionData.collaborators
                    )
                )
            } else {
                //Fetch data from API
                getDataOfPlaylist(mbid, onError = {
                    emitError(it)
                }, onSuccess = { playlist ->
                    createEditScreenUIStateFlow.emit(
                        createEditScreenUIStateFlow.value.copy(
                            playlistData = playlist,
                            isLoading = false,
                            name = playlist.title ?: "",
                            description = Utils.removeHtmlTags(playlist.annotation ?: ""),
                            isPublic = playlist.extension.playlistExtensionData.public ?: false,
                            playlistMBID = mbid,
                            collaboratorSelected = playlist.extension.playlistExtensionData.collaborators
                        )
                    )
                })
            }
        }
    }

    private fun getDataOfPlaylist(
        mbid: String?, onError: suspend (ResponseError?) -> Unit,
        onSuccess: suspend (PlaylistData) -> Unit
    ) {
        viewModelScope.launch(ioDispatcher) {
            val coverArtDeferred = async {
                if (mbid != null) {
                    val result = repository.getPlaylistCoverArt(mbid)
                    if (result.status == Resource.Status.SUCCESS) result.data
                    else {
                        onError(result.error)
                        null
                    }
                } else null
            }
            val result = repository.fetchPlaylist(mbid)
            val coverArt = coverArtDeferred.await()
            val playlist = result.data?.playlist?.copy(coverArt = coverArt)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (mbid == null) return@launch
                    if (playlist == null) onError(ResponseError.UNKNOWN)
                    playlistData.emit(playlistData.value + (mbid to playlist!!))
                    onSuccess(playlist)
                }

                Resource.Status.FAILED -> onError(result.error)
                else -> return@launch
            }
        }
    }

    fun editPlaylistScreenData(
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null,
        collaborators: List<String>? = null
    ) {
        viewModelScope.launch {
            createEditScreenUIStateFlow.emit(
                createEditScreenUIStateFlow.value.copy(
                    name = name ?: createEditScreenUIStateFlow.value.name,
                    description = description ?: createEditScreenUIStateFlow.value.description,
                    isPublic = isPublic ?: createEditScreenUIStateFlow.value.isPublic,
                    collaboratorSelected = collaborators?.distinct()?.filter { it != username }
                        ?: createEditScreenUIStateFlow.value.collaboratorSelected,
                    emptyTitleFieldError = false
                )
            )
        }
    }

    fun queryCollaborators(query: String) {
        viewModelScope.launch {
            userInputQueryFlow.emit(query)
        }
    }

    fun saveNewOrEditedPlaylist(onSuccess: (String) -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            val refreshPlaylistScreen = {
                getDataInPlaylistScreen(mbid = createEditScreenUIStateFlow.value.playlistMBID)
            }
            if (createEditScreenUIStateFlow.value.name.isEmpty()) {
                createEditScreenUIStateFlow.emit(
                    createEditScreenUIStateFlow.value.copy(
                        emptyTitleFieldError = true
                    )
                )
                return@launch
            }
            createEditScreenUIStateFlow.emit(createEditScreenUIStateFlow.value.copy(isSaving = true))

            val playlist = PlaylistData(
                title = createEditScreenUIStateFlow.value.name,
                annotation = createEditScreenUIStateFlow.value.description,
                extension = Extension(
                    playlistExtensionData = PlaylistExtensionData(
                        public = createEditScreenUIStateFlow.value.isPublic,
                        collaborators = createEditScreenUIStateFlow.value.collaboratorSelected
                    )
                )
            )

            if (createEditScreenUIStateFlow.value.playlistMBID == null)
                createPlaylist(
                    playlist, onSuccess = {
                        createEditScreenUIStateFlow.emit(
                            createEditScreenUIStateFlow.value.copy(
                                isSaving = false
                            )
                        )
                        onSuccess("Playlist Saved Successfully!!")
                        refreshPlaylistScreen()
                    },
                    onError = {
                        createEditScreenUIStateFlow.emit(
                            createEditScreenUIStateFlow.value.copy(
                                isSaving = false
                            )
                        )
                        emitError(it)
                    })
            else
                savePlaylist(
                    playlist,
                    createEditScreenUIStateFlow.value.playlistMBID!!,
                    onSuccess = {
                        createEditScreenUIStateFlow.emit(
                            createEditScreenUIStateFlow.value.copy(
                                isSaving = false
                            )
                        )
                        onSuccess("Playlist Saved Successfully!!")
                        //Saving data to in-memory cache
                        val playlists = playlistData.value.toMutableMap()
                        playlists[createEditScreenUIStateFlow.value.playlistMBID!!] = playlist
                        playlistData.emit(playlists)
                        refreshPlaylistScreen()
                    },
                    onError = {
                        emitError(it)
                        createEditScreenUIStateFlow.emit(
                            createEditScreenUIStateFlow.value.copy(
                                isSaving = false
                            )
                        )
                    })
        }
    }

    private fun savePlaylist(
        playlistData: PlaylistData, playlistMbid: String, onSuccess: suspend () -> Unit,
        onError: suspend (ResponseError?) -> Unit
    ) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.editPlaylist(
                PlaylistPayload(playlist = playlistData),
                playlistMbid = playlistMbid
            )
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    onSuccess()
                }

                Resource.Status.FAILED -> {
                    onError(result.error)
                }

                else -> return@launch
            }
        }
    }

    private fun createPlaylist(
        playlistData: PlaylistData,
        onSuccess: suspend () -> Unit,
        onError: suspend (ResponseError?) -> Unit
    ) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.addPlaylist(PlaylistPayload(playlist = playlistData))
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    onSuccess()
                }

                Resource.Status.FAILED -> {
                    onError(result.error)
                }

                else -> return@launch
            }
        }
    }

    fun addTrackToPlaylist(recordingData: RecordingData) {
        viewModelScope.launch {
            //Check whether recording is already added
            if (playlistScreenUIStateFlow.value.playlistData?.track?.any { it.getRecordingMBID() == recordingData.id } == true) {
                emitError(ResponseError.BAD_REQUEST.apply {
                    actualResponse = "Recording already added to the playlist"
                })
                return@launch
            }

            //Adding the recording to the UI intially
            val playlist = playlistScreenUIStateFlow.value.playlistData?.track?.plus(
                recordingData.toPlaylistTrack()
            )?.let {
                playlistScreenUIStateFlow.value.playlistData?.copy(
                    track = it
                )
            }
            playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(playlistData = playlist))

            //Adding the recording to the API
            val result = repository.addTracks(
                playlistScreenUIStateFlow.value.playlistMBID,
                listOf(recordingData.toPlaylistTrack())
            )
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    //Updating the playlist data in the UI
                    if (result.data?.status != "ok") {
                        emitError(ResponseError.UNKNOWN.apply {
                            actualResponse = "Some error occurred while adding the track"
                        })
                    } else {
                        //Refresh screen (to fetch cover art)
                        emitMsg(R.string.track_added_successfully)
                        if (playlistScreenUIStateFlow.value.playlistMBID != null)
                            getDataInPlaylistScreen(
                                playlistScreenUIStateFlow.value.playlistMBID!!,
                                isRefresh = true
                            )
                    }
                }

                Resource.Status.FAILED -> {
                    emitError(result.error)
                    //Removing if unsuccessful
                    val updatedPlaylist =
                        playlistScreenUIStateFlow.value.playlistData?.track?.filter { it.getRecordingMBID() != recordingData.id }
                            ?.let {
                                playlistScreenUIStateFlow.value.playlistData?.copy(
                                    track = it
                                )
                            }
                    playlistScreenUIStateFlow.emit(playlistScreenUIStateFlow.value.copy(playlistData = updatedPlaylist))
                }

                else -> return@launch
            }
        }
    }

    fun temporarilyMoveTrack(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            if (!playlistScreenUIStateFlow.value.isUserPlaylistOwner)
                return@launch
            val playlist = playlistScreenUIStateFlow.value.playlistData
            val track = playlist?.track?.get(fromIndex)
            val updatedTrack = playlist?.track?.toMutableList()
            updatedTrack?.removeAt(fromIndex)
            updatedTrack?.add(toIndex, track!!)
            playlistScreenUIStateFlow.emit(
                playlistScreenUIStateFlow.value.copy(
                    playlistData = playlist?.copy(
                        track = updatedTrack?.toList() ?: emptyList()
                    )
                )
            )
        }
    }

    fun reorderPlaylist(moveTrack: MoveTrack) {
        viewModelScope.launch {
            val refreshPlaylistScreen = {
                if (playlistScreenUIStateFlow.value.playlistMBID != null)
                    getDataInPlaylistScreen(
                        playlistScreenUIStateFlow.value.playlistMBID!!,
                        isRefresh = true
                    )
            }
            if (!playlistScreenUIStateFlow.value.isUserPlaylistOwner || (moveTrack.from == moveTrack.to))
                return@launch
            val result = repository.moveTrack(
                playlistScreenUIStateFlow.value.playlistMBID,
                moveTrack
            )
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (result.data?.status != "ok") {
                        emitError(ResponseError.UNKNOWN.apply {
                            actualResponse = "Some error occurred while moving the track"
                        })
                        refreshPlaylistScreen()
                    } else {
                        emitMsg(R.string.track_moved_successfully)
                        refreshPlaylistScreen()
                    }
                }

                Resource.Status.FAILED -> {
                    emitError(result.error)
                    refreshPlaylistScreen()
                }

                else -> return@launch
            }
        }
    }

    fun changeAddTrackBottomSheetState(isVisible: Boolean) {
        viewModelScope.launch {
            playlistScreenUIStateFlow.emit(
                playlistScreenUIStateFlow.value.copy(
                    isAddTrackBottomSheetVisible = isVisible
                )
            )
        }
    }

    fun queryRecordings(query: String) {
        viewModelScope.launch {
            recordingInputQueryFlow.emit(query)
        }
    }

    //This function saves the playlist to the user's account
    fun duplicatePlaylist(
        playlistMbid: String?
    ) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.copyPlaylist(playlistMbid)
            if (result.status == Resource.Status.SUCCESS) {
                emitMsg(R.string.playlist_duplicated_successfully)
            } else {
                emitError(result.error)
            }
        }
    }


    override val uiState: StateFlow<PlaylistDataUIState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<PlaylistDataUIState> {
        return combine(
            successMsgFlow,
            createEditScreenUIStateFlow,
            playlistScreenUIStateFlow,
            userInputQueryFlow,
            userListFlow,
            errorFlow,
            recordingInputQueryFlow
        ) { array ->
            PlaylistDataUIState(
                error = array[5] as ResponseError?,
                playlistDetailUIState = (array[2] as PlaylistDetailUIState).copy(
                    queryText = array[6] as String?
                ),
                createEditScreenUIState = (array[1] as CreateEditScreenUIState).copy(
                    collaboratorQueryText = array[3] as String,
                    usersSearched = array[4] as List<User>
                ),
                successMsg = array[0] as Int?
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            PlaylistDataUIState()
        )
    }

    //Select playlist bottom sheet functions

    val userPlaylistPager: Flow<PagingData<UserPlaylist>> = Pager(
        PagingConfig(
            pageSize = PlaylistDataRepository.USER_PLAYLISTS_FETCH_COUNT,
            enablePlaceholders = false
        )
    ){
        UserPlaylistPagingSource(
            username = username,
            onError = {
                emitError(it)
            },
            shouldFetchCoverArt = false,
            playlistRepository = repository,
            ioDispatcher = ioDispatcher
        )
    }   .flow
        .cachedIn(viewModelScope)

    val collabPlaylistPager: Flow<PagingData<UserPlaylist>> = Pager(
        PagingConfig(
            pageSize = PlaylistDataRepository.COLLAB_PLAYLISTS_FETCH_COUNT,
            enablePlaceholders = false
        )
    ){
        CollabPlaylistPagingSource(
            username = username,
            onError = {emitError(it)},
            shouldFetchCoverArt = false,
            playlistDataRepository = repository,
            ioDispatcher = ioDispatcher
        )

    }
        .flow
        .cachedIn(viewModelScope)

    fun addTrackToPlaylistFromSelectPlaylist(
        songMetadata: Metadata,
        playlistMbid: String?
    ) {
        viewModelScope.launch {
            if (playlistMbid == null) return@launch
            val result = repository.addTracks(
                playlistMbid,
                listOf(
                    PlaylistTrack.fromMetadata(songMetadata)
                )
            )
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (result.data?.status != "ok") {
                        emitError(ResponseError.UNKNOWN.apply {
                            actualResponse = "Some error occurred while adding the track"
                        })
                    } else {
                        emitMsg(R.string.track_added_successfully)
                    }
                }

                Resource.Status.FAILED -> emitError(result.error)
                else -> return@launch
            }
        }
    }
}