package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.RecommendationMetadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.ReviewMetadata
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import kotlin.coroutines.CoroutineContext

abstract class SocialViewModel<UiState> (
    private val repository: SocialRepository,
    private val appPreferences: AppPreferences,
    private val ioDispatcher: CoroutineDispatcher,
): BaseViewModel<UiState>() {
    
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
                username = appPreferences.username,
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
                username = appPreferences.username,
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
                username = appPreferences.username,
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