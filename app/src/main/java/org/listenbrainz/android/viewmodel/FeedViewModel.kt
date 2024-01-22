package org.listenbrainz.android.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventDeletionData
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.FeedEventType.Companion.isActionDelete
import org.listenbrainz.android.model.feed.FeedEventVisibilityData
import org.listenbrainz.android.repository.feed.FeedRepository
import org.listenbrainz.android.repository.feed.FeedRepository.Companion.FeedEventCount
import org.listenbrainz.android.repository.feed.FeedRepository.Companion.FeedListensCount
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.ui.screens.feed.FeedUiEventData
import org.listenbrainz.android.ui.screens.feed.FeedUiEventItem
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.FollowListensPagingSource
import org.listenbrainz.android.ui.screens.feed.MyFeedPagingSource
import org.listenbrainz.android.ui.screens.feed.SimilarListensPagingSource
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.Log.d
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val socialRepository: SocialRepository,
    private val listensRepository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
): BaseViewModel<FeedUiState>() {
    
    // Search follower flow
    private val inputSearchFollowerQuery = MutableStateFlow("")
    @OptIn(FlowPreview::class)
    private val searchFollowerQuery = inputSearchFollowerQuery.asStateFlow().debounce(500).distinctUntilChanged()
    private val searchFollowerResult = MutableStateFlow<List<String>>(emptyList())
    
    // My Feed
    private val myFeedPager: Flow<PagingData<FeedUiEventItem>> = Pager(
        PagingConfig(pageSize = FeedEventCount),
        initialKey = null
    ) { createNewMyFeedPagingSource() }
        .flow
        .cachedIn(viewModelScope)
    
    private val isDeletedMap: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    private val isHiddenMap: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    private val myFeedFlow = MutableStateFlow(FeedUiEventData(isHiddenMap, isDeletedMap, myFeedPager))
    
    
    // Follow Listens
    private val followListensPager: Flow<PagingData<FeedUiEventItem>> = Pager(
        PagingConfig(pageSize = FeedListensCount),
        initialKey = null
    ) { createNewFollowListensPagingSource() }
        .flow
        .cachedIn(viewModelScope)
    private val followListensFlow = MutableStateFlow(FeedUiEventData(eventList = followListensPager))
    
    
    // Similar Listens
    private val similarListensPager: Flow<PagingData<FeedUiEventItem>> = Pager(
        PagingConfig(pageSize = FeedListensCount),
        initialKey = null
    ) { createNewSimilarListensPagingSource() }
        .flow
        .cachedIn(viewModelScope)
    private val similarListensFlow = MutableStateFlow(FeedUiEventData(eventList = similarListensPager))
    
    // Exposed UI state
    override val uiState = createUiStateFlow()
    
    init {
        viewModelScope.launch(defaultDispatcher) {
            searchFollowerQuery.collectLatest { query ->
                if (query.isEmpty()) return@collectLatest
                
                val result = socialRepository.getFollowers(appPreferences.username.get())
                if (result.status == Resource.Status.SUCCESS){
                    searchFollowerResult.emit(
                        result.data?.followers?.filter {
                            it.startsWith(query, ignoreCase = true) || it.contains(query, ignoreCase = true)
                        } ?: emptyList()
                    )
                    println(searchFollowerResult.value)
                } else {
                    emitError(error = result.error)
                }
                
            }
        }
        
    }
    
    override fun createUiStateFlow(): StateFlow<FeedUiState> {
        return combine(
            myFeedFlow,
            followListensFlow,
            similarListensFlow,
            searchFollowerResult,
            errorFlow
        ){ feedScreenState, followListensState, similarListensState, searchResult, error ->
            FeedUiState(feedScreenState, followListensState, similarListensState, searchResult, error)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            FeedUiState()
        )
    }
    
    private fun createNewMyFeedPagingSource(): MyFeedPagingSource =
        MyFeedPagingSource(
            username = { appPreferences.username.get() },
            addEntryToMap = { id, value ->
                isHiddenMap[id] = value
            },
            onError =  { error ->
                emitError(error)
            },
            feedRepository = feedRepository,
            ioDispatcher = ioDispatcher
        )
    
    private fun createNewFollowListensPagingSource(): FollowListensPagingSource =
        FollowListensPagingSource(
            username = { appPreferences.username.get() } ,
            onError =  { error ->
                emitError(error)
            },
            feedRepository = feedRepository,
            ioDispatcher = ioDispatcher
        )
    
    private fun createNewSimilarListensPagingSource(): SimilarListensPagingSource =
        SimilarListensPagingSource(
            username = { appPreferences.username.get() },
            onError =  { error ->
                emitError(error)
            },
            feedRepository = feedRepository,
            ioDispatcher = ioDispatcher
        )
    
    fun play(event: FeedEvent) {
        val spotifyId = event.metadata.trackMetadata?.additionalInfo?.spotifyId
        if (spotifyId != null){
            Uri.parse(spotifyId).lastPathSegment?.let { trackId ->
                remotePlaybackHandler.playUri(trackId){
                    playFromYoutubeMusic(event)
                }
            }
        } else {
            playFromYoutubeMusic(event)
        }
    }
    
    private fun playFromYoutubeMusic(event: FeedEvent) {
        viewModelScope.launch {
            if (event.metadata.trackMetadata != null){
                remotePlaybackHandler.apply {
                    val result = playOnYoutube {
                        withContext(ioDispatcher) {
                            searchYoutubeMusicVideoId(
                                event.metadata.trackMetadata.trackName,
                                event.metadata.trackMetadata.artistName
                            )
                        }
                    }
                    
                    if (result.status == Resource.Status.SUCCESS){
                        d("Play on youtube music successful")
                    } else {
                        emitError(ResponseError.REMOTE_PLAYER_ERROR.apply { actualResponse = "Could not play the requested track." })
                    }
                }
            } else {
                // Could not play song.
                emitError(ResponseError.REMOTE_PLAYER_ERROR.apply { actualResponse = "Could not play the requested track." })
            }
        }
    }
    
    fun searchUser(query: String){
        viewModelScope.launch {
            inputSearchFollowerQuery.emit(query)
        }
    }
    
    suspend fun isCritiqueBrainzLinked(): Boolean? {
        val result = listensRepository.getLinkedServices(
            appPreferences.lbAccessToken.get(),
            appPreferences.username.get()
        )
        if (!result.status.isSuccessful()) {
            emitError(result.error)
        }
        return result.data?.toLinkedServicesList()?.contains(LinkedService.CRITIQUEBRAINZ)
    }
    
    fun hideOrDeleteEvent(event: FeedEvent, eventType: FeedEventType, parentUser: String) {
        
        viewModelScope.launch(defaultDispatcher) {
            if (isActionDelete(event, eventType, parentUser)){
                deleteEvent(event.id!!, event.type)
            } else if (isHiddenMap[event.id] == true){
                unhideEvent(data = FeedEventVisibilityData(event.type, event.id.toString()))
            } else {
                // null means false
                hideEvent(data = FeedEventVisibilityData(event.type, event.id.toString()))
            }
            
        }
    }
    
    private suspend fun deleteEvent(id: Int, type: String) {
    
        // Optimistically inverting state
        isDeletedMap[id] = true
    
        val result = withContext(ioDispatcher) {
            if (type == FeedEventType.RECORDING_PIN.type) {
                socialRepository.deletePin(id)
            } else {
                feedRepository.deleteEvent(
                    appPreferences.username.get(),
                    FeedEventDeletionData(eventId = id.toString(), eventType = type)
                )
            }
        }
    
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                isDeletedMap[id] = false
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    
    private suspend fun hideEvent(data: FeedEventVisibilityData) {
        
        // Optimistically toggle
        toggleHiddenStatus(data)
        
        val result = withContext(ioDispatcher) {
            feedRepository.hideEvent(appPreferences.username.get(), data)
        }
        
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                toggleHiddenStatus(data)
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    private suspend fun unhideEvent(data: FeedEventVisibilityData) {
        
        // Optimistically toggle
        toggleHiddenStatus(data)
        
        val result = withContext(ioDispatcher) {
            feedRepository.unhideEvent(appPreferences.username.get(), data)
        }
        
        when (result.status) {
            Resource.Status.FAILED -> {
                // Toggle back on failure.
                toggleHiddenStatus(data)
                errorFlow.emit(result.error)
            }
            else -> Unit
        }
        
    }
    
    private suspend fun toggleHiddenStatus(data: FeedEventVisibilityData) {
        try {
            val currentState = isHiddenMap[data.eventId!!.toInt()]
            isHiddenMap[data.eventId!!.toInt()] = currentState == null || currentState == false
        } catch (e: Exception) {
            errorFlow.emit(ResponseError.UNKNOWN)
            e.printStackTrace()
        }
    }
    
}