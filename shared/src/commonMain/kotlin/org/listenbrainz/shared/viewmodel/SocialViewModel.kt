package org.listenbrainz.shared.viewmodel

import androidx.lifecycle.viewModelScope
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
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.shared.model.RecommendationData
import org.listenbrainz.shared.model.RecommendationMetadata
import org.listenbrainz.shared.model.ResponseError
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.ReviewMetadata
import org.listenbrainz.shared.model.SocialData
import org.listenbrainz.shared.model.SocialUiState
import org.listenbrainz.shared.model.TrackMetadata
import org.listenbrainz.shared.model.feed.ReviewEntityType
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.shared.repository.social.SocialRepository
import org.listenbrainz.shared.util.Resource
import org.listenbrainz.shared.util.StringProvider
import org.listenbrainz.shared.util.StringResource

class SocialViewModel(
    private val repository: SocialRepository,
    private val listensRepository: ListensRepository,
    private val appPreferences: AppPreferences,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher,
    private val stringProvider: StringProvider
) : FollowUnfollowModel<SocialUiState>(repository, ioDispatcher) {

    private val inputSearchFollowerQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val searchFollowerQuery =
        inputSearchFollowerQuery.asStateFlow().debounce(500).distinctUntilChanged()
    private val searchFollowerResult = MutableStateFlow<List<String>>(emptyList())

    override val uiState: StateFlow<SocialUiState> = createUiStateFlow()

    init {
        viewModelScope.launch(defaultDispatcher) {
            searchFollowerQuery.collectLatest { query ->
                if (query.isEmpty()) return@collectLatest

                val result = repository.getFollowers(appPreferences.username.get())
                if (result.status == Resource.Status.SUCCESS) {
                    searchFollowerResult.emit(
                        result.data?.followers?.filter {
                            it.startsWith(query, ignoreCase = true) || it.contains(
                                query,
                                ignoreCase = true
                            )
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
        ) { searchResult, error, message ->
            SocialUiState(searchResult, error, message)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            SocialUiState()
        )

    fun searchUser(query: String) {
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
        if (spotifyId != null) {
            val trackId = spotifyId.substringBefore('?').substringAfterLast('/').substringAfterLast(':')
            if(trackId.isNotEmpty()){
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
                                ?: return@withContext Resource.Companion.failure(ResponseError.DoesNotExist()),
                            trackMetadata.artistName.orEmpty()
                        )
                    }
                }
            }
        }
    }

    fun play() {
        remotePlaybackHandler.play()
    }

    fun pause() {
        remotePlaybackHandler.pause()
    }

    fun recommend(metadata: Metadata) {
        viewModelScope.launch(ioDispatcher) {
            val trackMetadata = metadata.trackMetadata ?: return@launch
            val result = repository.postRecommendationToAll(
                username = appPreferences.username.get(),
                data = RecommendationData(
                    metadata = RecommendationMetadata(
                        trackName = trackMetadata.trackName ?: return@launch,
                        artistName = trackMetadata.artistName.orEmpty(),
                        releaseName = trackMetadata.releaseName,
                        recordingMbid = trackMetadata.mbidMapping?.recordingMbid,
                        recordingMsid = trackMetadata.additionalInfo?.recordingMsid
                    )
                )
            )

            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(stringProvider.getString(StringResource.RECOMMENDATION_GREETING))
            }
        }
    }

    fun personallyRecommend(metadata: Metadata, users: List<String>, blurbContent: String) {
        viewModelScope.launch(ioDispatcher) {
            val trackMetadata = metadata.trackMetadata ?: return@launch
            val result = repository.postPersonalRecommendation(
                username = appPreferences.username.get(),
                data = RecommendationData(
                    metadata = RecommendationMetadata(
                        trackName = trackMetadata.trackName ?: return@launch,
                        artistName = trackMetadata.artistName.orEmpty(),
                        releaseName = trackMetadata.releaseName,
                        recordingMbid = trackMetadata.mbidMapping?.recordingMbid,
                        recordingMsid = trackMetadata.additionalInfo?.recordingMsid,
                        users = users,
                        blurbContent = blurbContent
                    )
                )
            )

            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(stringProvider.getString(StringResource.PERSONAL_RECOMMENDATION_GREETING))
            }
        }
    }

    fun review(
        metadata: Metadata,
        entityType: ReviewEntityType,
        blurbContent: String,
        rating: Int?,
        locale: String
    ) {
        viewModelScope.launch(ioDispatcher) {
            val trackMetadata = metadata.trackMetadata ?: return@launch
            val mbidMapping = trackMetadata.mbidMapping ?: return@launch
            val result = repository.postReview(
                username = appPreferences.username.get(),
                data = Review(
                    metadata = ReviewMetadata(
                        entityName = trackMetadata.trackName ?: return@launch,
                        entityId = when (entityType) {
                            ReviewEntityType.RECORDING -> (mbidMapping.recordingMbid
                                ?: return@launch).toString()

                            ReviewEntityType.ARTIST -> (when (mbidMapping.artistMbids.size) {
                                1 -> mbidMapping.artistMbids[0]
                                else -> return@launch
                            })

                            ReviewEntityType.RELEASE_GROUP -> (mbidMapping.recordingMbid
                                ?: return@launch).toString()
                        },
                        entityType = entityType.code,
                        text = blurbContent,
                        rating = rating,
                        language = locale
                    )
                )
            )

            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(stringProvider.getString(StringResource.REVIEW_GREETING))
            }
        }
    }

    fun pin(metadata: Metadata, blurbContent: String?) {
        viewModelScope.launch(ioDispatcher) {
            val result = repository.pin(
                recordingMsid = metadata.trackMetadata?.additionalInfo?.recordingMsid,
                recordingMbid = metadata.trackMetadata?.mbidMapping?.recordingMbid,
                blurbContent = blurbContent
            )

            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(stringProvider.getString(StringResource.PIN_GREETING))
            }
        }
    }

    suspend fun getFollowers(): Resource<SocialData> {
        val username = appPreferences.username.get()
        return repository.getFollowers(username).also {
            if (it.status == Resource.Status.FAILED) {
                emitError(it.error)
            }
        }
    }
    fun deleteListen(metadata: Metadata) {
        viewModelScope.launch(ioDispatcher) {
            val listenedAt = metadata.listenedAt
            if(listenedAt == null){
                return@launch
            }
            val msid = metadata.trackMetadata?.additionalInfo?.recordingMsid
            if(msid ==null){
                emitError(ResponseError.BadRequest())
                return@launch
            }
            val result = listensRepository.deleteListen(listenedAt, msid)
            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(stringProvider.getString(StringResource.LISTEN_DELETED))
            }

        }
    }
}