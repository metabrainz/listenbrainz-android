package org.listenbrainz.android.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.RecommendationMetadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.ReviewMetadata
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val appPreferences: AppPreferences,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
): BaseViewModel<SocialUiState>() {
    
    override val uiState: StateFlow<SocialUiState> = createUiStateFlow()
    override fun createUiStateFlow(): StateFlow<SocialUiState> =
        errorFlow.map {
            SocialUiState(it)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SocialUiState()
        )
    
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
                            trackMetadata.trackName,
                            trackMetadata.artistName
                        )
                    }
                }
            }
        }
    }
    
    fun play(){
        remotePlaybackHandler.play()
    }
    
    fun pause(){
        remotePlaybackHandler.pause()
    }
    
    fun toggleFollowStatus(user: User, index: Int, currentStatus: Boolean, invertFollowUiState: (index: Int) -> Unit) {
        viewModelScope.launch(defaultDispatcher) {
            
            if (user.username.isEmpty()) return@launch
            
            try {
                if (currentStatus)
                    coroutineContext.optimisticallyUnfollowUser(user, index) { invertFollowUiState(it) }
                else
                    coroutineContext.optimisticallyFollowUser(user, index) { invertFollowUiState(it) }
            } catch (e: CancellationException) {
                e.printStackTrace()
            }
            
        }
    }
    
    /**
     * @param [invertUiState] **should** check for cancellation internally.*/
    suspend fun CoroutineContext.optimisticallyFollowUser(
        user: User,
        index: Int,
        invertUiState: CoroutineContext.(index: Int) -> Unit
    ) {
        /** Determines if user is followed or not.*/
        fun userIsAlreadyFollowed(error: ResponseError?): Boolean {
            return error == ResponseError.BAD_REQUEST &&
                    error.actualResponse?.contains("already following") == true
        }
        
        invertUiState(index)
        
        val result = withContext(ioDispatcher) {
            repository.followUser(user.username)
        }
        when (result.status) {
            Resource.Status.FAILED -> {
                emitError(result.error)
                
                if (userIsAlreadyFollowed(result.error)){
                    // We won't toggle back follow state if user is already followed.
                    return
                }
                
                invertUiState(index)
            }
            else -> Unit
        }
    }
    
    /**
     * @param [invertUiState] **should** check for cancellation internally.*/
    suspend fun CoroutineContext.optimisticallyUnfollowUser(
        user: User,
        index: Int,
        invertUiState: CoroutineContext.(index: Int) -> Unit
    ) {
        
        invertUiState(index)
        
        val result = withContext(ioDispatcher) {
            repository.unfollowUser(user.username)
        }
        return when (result.status) {
            Resource.Status.FAILED -> {
                // Since same response is given by server even if user is unfollowed or not, we
                // won't do anything here.
                invertUiState(index)
                emitError(result.error)
            }
            else -> Unit
        }
    }
    
    fun recommend(metadata: Metadata) {
        
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postRecommendationToAll(
                username = appPreferences.getUsername(),
                data = RecommendationData(
                    metadata = RecommendationMetadata(
                        trackName = metadata.trackMetadata?.trackName ?: return@launch,
                        artistName = metadata.trackMetadata.artistName,
                        releaseName = metadata.trackMetadata.releaseName,
                        recordingMbid = metadata.trackMetadata.mbidMapping?.recordingMbid,
                        recordingMsid = metadata.trackMetadata.additionalInfo?.recordingMsid
                    )
                )
            )
            
            if (result.status == Resource.Status.FAILED){
                emitError(result.error)
            }
        }
    }
    
    fun personallyRecommend(metadata: Metadata, users: List<String>, blurbContent: String) {
        
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postPersonalRecommendation(
                username = appPreferences.getUsername(),
                data = RecommendationData(
                    metadata = RecommendationMetadata(
                        trackName = metadata.trackMetadata?.trackName ?: return@launch,
                        artistName = metadata.trackMetadata.artistName,
                        releaseName = metadata.trackMetadata.releaseName,
                        recordingMbid = metadata.trackMetadata.mbidMapping?.recordingMbid,
                        recordingMsid = metadata.trackMetadata.additionalInfo?.recordingMsid,
                        users = users,
                        blurbContent = blurbContent
                    )
                )
            )
            
            if (result.status == Resource.Status.FAILED){
                emitError(result.error)
            }
        }
        
    }
    
    fun review(metadata: Metadata, entityType: ReviewEntityType, blurbContent: String, rating: Int?, locale: String){
        
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postReview(
                username = appPreferences.getUsername(),
                data = Review(
                    metadata = ReviewMetadata(
                        entityName = metadata.trackMetadata?.trackName ?: return@launch,
                        entityId = (metadata.trackMetadata.mbidMapping?.recordingMbid ?: return@launch).toString(),
                        entityType = entityType.code,
                        text = blurbContent,
                        rating = rating,
                        language = locale
                    )
                )
            )
            
            if (result.status == Resource.Status.FAILED){
                emitError(result.error)
            }
        }
    }
    
    fun pin(metadata: Metadata, blurbContent: String? ) {
        
        viewModelScope.launch(ioDispatcher) {
            val result = repository.pin(
                recordingMsid = metadata.trackMetadata?.additionalInfo?.recordingMsid,
                recordingMbid = metadata.trackMetadata?.mbidMapping?.recordingMbid,
                blurbContent = blurbContent
            )
            
            if (result.status == Resource.Status.FAILED){
                emitError(result.error)
            }
        }
    }
}