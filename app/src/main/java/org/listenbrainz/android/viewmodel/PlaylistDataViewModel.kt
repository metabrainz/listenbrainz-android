package org.listenbrainz.android.viewmodel

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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.playlist.Extension
import org.listenbrainz.android.model.playlist.PlaylistData
import org.listenbrainz.android.model.playlist.PlaylistExtensionData
import org.listenbrainz.android.model.playlist.PlaylistPayload
import org.listenbrainz.android.repository.playlists.PlaylistDataRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.ui.screens.playlist.CreateEditScreenUIState
import org.listenbrainz.android.ui.screens.playlist.PlaylistDataUIState
import org.listenbrainz.android.util.Resource
import org.listenbrainz.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class PlaylistDataViewModel @Inject constructor(
    val appPreferences: AppPreferences,
    private val repository: PlaylistDataRepository,
    private val socialRepository: SocialRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<PlaylistDataUIState>() {
    private var username: String? = null
    private val inputQueryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val queryFlow = inputQueryFlow.asStateFlow().debounce(500).distinctUntilChanged()
    private val userListFlow = MutableStateFlow<List<User>>(emptyList())
    private val playlistData = MutableStateFlow<Map<String, PlaylistData>>(emptyMap())

    private val createEditScreenUIStateFlow: MutableStateFlow<CreateEditScreenUIState> =
        MutableStateFlow(
            CreateEditScreenUIState()
        )

    init {
        viewModelScope.launch(ioDispatcher) {
            queryFlow.collectLatest { username ->
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

    fun getInitialData(mbid: String?) {
        var playlist = PlaylistData()
        viewModelScope.launch(ioDispatcher) {
            username = appPreferences.username.get()
            if (mbid == null) {
                createEditScreenUIStateFlow.emit(createEditScreenUIStateFlow.value.copy(isLoading = false))
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
            val result = repository.fetchPlaylist(mbid)
            when (result.status) {
                Resource.Status.SUCCESS -> {
                    if (mbid == null) return@launch
                    if (result.data?.playlist == null) onError(ResponseError.UNKNOWN)
                    playlistData.emit(playlistData.value + (mbid to result.data?.playlist!!))
                    onSuccess(result.data.playlist)
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
            inputQueryFlow.emit(query)
        }
    }

    fun saveNewOrEditedPlaylist(onSuccess: (String) -> Unit) {
        viewModelScope.launch(ioDispatcher) {
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
                createPlaylist(playlist, onSuccess = {
                    createEditScreenUIStateFlow.emit(
                        createEditScreenUIStateFlow.value.copy(
                            isSaving = false
                        )
                    )
                    onSuccess("Playlist Saved Successfully!!")
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

    override val uiState: StateFlow<PlaylistDataUIState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<PlaylistDataUIState> {
        return combine(
            createEditScreenUIStateFlow,
            inputQueryFlow,
            userListFlow,
            errorFlow
        ) { createUiState, inputQuery, users, errorFlow ->
            PlaylistDataUIState(
                error = errorFlow,
                createEditScreenUIState = createUiState.copy(
                    collaboratorQueryText = inputQuery,
                    usersSearched = users
                )
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            PlaylistDataUIState()
        )
    }
}