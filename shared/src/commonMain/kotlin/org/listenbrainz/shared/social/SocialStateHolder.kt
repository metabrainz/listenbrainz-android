package org.listenbrainz.shared.social

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.shared.model.Review
import org.listenbrainz.shared.model.ReviewEntityType
import org.listenbrainz.shared.model.ReviewMetadata
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.shared.repository.ListensRepository
import org.listenbrainz.shared.repository.SocialRepository

class SocialStateHolder(
    private val scope: CoroutineScope,
    private val repository: SocialRepository,
    val appPreferences: AppPreferences,
    val listensRepository: ListensRepository,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val searchFollowerResult = MutableStateFlow<List<String>>(emptyList())
    private val errorFlow = MutableStateFlow<ResponseError?>(null)
    private val successMsgFlow = MutableStateFlow<Int?>(null)

    private val inputSearchFollowerQuery = MutableStateFlow("")

    val uiState: StateFlow<SocialUiState> =
        combine(
            searchFollowerResult,
            errorFlow,
            successMsgFlow
        ) { followers, error, message ->
            SocialUiState(
                searchResult = followers,
                error = error,
                successMsgId = message
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            SocialUiState()
        )
    @OptIn(FlowPreview::class)
    private val searchFollowerQuery =
        inputSearchFollowerQuery.debounce(500).distinctUntilChanged()

    init {
        observeSearch()
    }

    fun searchUser(query: String) {
        inputSearchFollowerQuery.value = query
    }

    private fun observeSearch() {
        scope.launch(ioDispatcher) {
            searchFollowerQuery.collectLatest { query ->
                searchQuery(query)
            }
        }
    }

    private suspend fun searchQuery(query: String) {

        if (query.isBlank()) {
            searchFollowerResult.value = emptyList()
            return
        }

        val result = repository.getFollowers(appPreferences.username.get())

        if (result.status == Resource.Status.SUCCESS) {

            val followers = result.data?.followers ?: emptyList()

            val filtered = followers.filter {
                it.startsWith(query, ignoreCase = true) ||
                        it.contains(query, ignoreCase = true)
            }

            searchFollowerResult.value = filtered

        } else {
            errorFlow.value = result.error
        }
    }

    suspend fun isCritiqueBrainzLinked(): Boolean? {
        val result = listensRepository.getLinkedServices(
            appPreferences.lbAccessToken.get(),
            appPreferences.username.get()
        )
        if (!result.status.isSuccessful()) {
            errorFlow.value = result.error
        }
        return result.data?.toLinkedServicesList()?.contains(LinkedService.CRITIQUEBRAINZ)
    }
    suspend fun getFollowers(): Resource<SocialData> {
        val username = appPreferences.username.get()
        return repository.getFollowers(username).also {
            if (it.status == Resource.Status.FAILED) {
                emitError(it.error)
            }
        }
    }

    fun recommend(metadata: Metadata) {
        scope.launch(ioDispatcher) {
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
                emitMsg(SocialMsg.RECOMMEND_SUCCESS)
            }
        }
    }
    fun pin(metadata: Metadata,blurbContent: String?){
        scope.launch(ioDispatcher){
            val result = repository.pin(
                recordingMbid = metadata.trackMetadata?.mbidMapping?.recordingMbid,
                recordingMsid = metadata.trackMetadata?.additionalInfo?.recordingMsid,
                blurbContent = blurbContent
            )
            if (result.status == Resource.Status.FAILED) {
                emitError(result.error)
            } else if (result.status == Resource.Status.SUCCESS) {
                emitMsg(SocialMsg.PIN_SUCCESS)
            }
        }

    }

    fun personallyRecommend(metadata: Metadata, users: List<String>, blurbContent: String) {
        scope.launch(ioDispatcher){
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
                emitMsg(SocialMsg.PERSONAL_RECOMMEND_SUCCESS)
            }
        }
    }
    fun review(metadata: Metadata, blurbContent: String, rating: Int?, locale: String,entityType: ReviewEntityType){
        scope.launch(ioDispatcher) {
            val trackMetadata = metadata.trackMetadata ?: return@launch
            val mbidMapping = trackMetadata.mbidMapping ?: return@launch
            val result = repository.postReview(
                username = appPreferences.username.get(),
                data = Review(
                        metadata = ReviewMetadata(
                            entityName = trackMetadata.trackName?:return@launch,
                            entityId = when(entityType){
                                ReviewEntityType.RECORDING -> (mbidMapping.recordingMbid ?: return@launch).toString()
                                ReviewEntityType.ARTIST -> (when(mbidMapping.artistMbids.size){
                                    1 -> mbidMapping.artistMbids[0]
                                    else -> return@launch
                                })
                                ReviewEntityType.RELEASE_GROUP -> (mbidMapping.recordingMbid ?: return@launch).toString()
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
                emitMsg(SocialMsg.REVIEW_SUCCESS)
            }
        }
    }
    private fun emitError(error: ResponseError?) {
        errorFlow.value = error
    }

    private fun emitMsg(messageId: Int?) {
        successMsgFlow.value = messageId
    }

}