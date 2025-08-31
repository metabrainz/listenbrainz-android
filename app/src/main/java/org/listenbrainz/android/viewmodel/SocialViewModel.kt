package org.listenbrainz.android.viewmodel

import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.withContext
import org.listenbrainz.android.R
import org.listenbrainz.android.di.DefaultDispatcher
import org.listenbrainz.android.di.IoDispatcher
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.RecommendationMetadata
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.ReviewMetadata
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.LinkedService
import org.listenbrainz.android.util.Resource
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val listensRepository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
): FollowUnfollowModel<SocialUiState>(repository, ioDispatcher) {

    private val inputSearchFollowerQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val searchFollowerQuery = inputSearchFollowerQuery.asStateFlow().debounce(500).distinctUntilChanged()
    private val searchFollowerResult = MutableStateFlow<List<String>>(emptyList())

    override val uiState: StateFlow<SocialUiState> = createUiStateFlow()

    init {
        viewModelScope.launch(defaultDispatcher) {
            searchFollowerQuery.collectLatest { query ->
                if (query.isEmpty()) return@collectLatest

                val result = repository.getFollowers(appPreferences.username.get())
                if (result.status == Resource.Status.SUCCESS){
                    searchFollowerResult.emit(
                        result.data?.followers?.filter {
                            it.startsWith(query, ignoreCase = true) || it.contains(query, ignoreCase = true)
                        } ?: emptyList()
                    )
                } else {
                    emitError(error = result.error)
                }
            }
        }
    }

    override fun createUiStateFlow(): StateFlow<SocialUiState> =
        combine(
            searchFollowerResult,
            errorFlow,
            successMsgFlow
        ){ searchResult, error, message ->
            SocialUiState(searchResult, error, message)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SocialUiState()
        )

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

    fun recommend(metadata: Metadata) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postRecommendationToAll(
                username = appPreferences.username.get(),
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
            else if(result.status == Resource.Status.SUCCESS){
                emitMsg(R.string.recommendation_greeting)
            }
        }
    }

    fun personallyRecommend(metadata: Metadata, users: List<String>, blurbContent: String) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postPersonalRecommendation(
                username = appPreferences.username.get(),
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
            else if(result.status == Resource.Status.SUCCESS){
                emitMsg(R.string.personal_recommendation_greeting)
            }
        }
    }

    fun review(metadata: Metadata, entityType: ReviewEntityType, blurbContent: String, rating: Int?, locale: String){
        viewModelScope.launch(ioDispatcher) {
            val result = repository.postReview(
                username = appPreferences.username.get(),
                data = Review(
                    metadata = ReviewMetadata(
                        entityName = metadata.trackMetadata?.trackName ?: return@launch,
                        entityId = when(entityType) {
                            ReviewEntityType.RECORDING -> (metadata.trackMetadata.mbidMapping?.recordingMbid ?: return@launch).toString()
                            ReviewEntityType.ARTIST -> (when(metadata.trackMetadata.mbidMapping?.artistMbids?.size){
                                1 -> metadata.trackMetadata.mbidMapping.artistMbids[0]
                                else -> return@launch
                            }).toString()
                            ReviewEntityType.RELEASE_GROUP -> (metadata.trackMetadata.mbidMapping?.recordingMbid ?: return@launch).toString() },
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
            else if(result.status == Resource.Status.SUCCESS){
                emitMsg(R.string.review_greeting)
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
            else if(result.status == Resource.Status.SUCCESS){
                emitMsg(R.string.pin_greeting)
            }
        }
    }

    suspend fun getFollowers(): Resource<SocialData> {
        val username = appPreferences.username.get()
        return repository.getFollowers(username).also {
            if(it.status == Resource.Status.FAILED){
                emitError(it.error)
            }
        }
    }
}