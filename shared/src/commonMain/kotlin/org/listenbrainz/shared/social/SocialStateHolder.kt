package org.listenbrainz.shared.social

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.listenbrainz.shared.model.LinkedService
import org.listenbrainz.shared.model.Metadata
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
            if(it.status== Resource.Status.FAILED){
//                Handle error based on BaseViewModel
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

            if (result.status == Resource.Status.FAILED){

            }
            else if(result.status == Resource.Status.SUCCESS){

            }
        }
    }
    fun pin(metadata: Metadata,blurbContent: String?){
//        will be implemented later after full SocialRepository migration
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

            if (result.status == Resource.Status.FAILED){

            }
            else if(result.status == Resource.Status.SUCCESS){

            }
        }
    }
    fun review(metadata: Metadata, blurbContent: String, rating: Int?, locale: String){
        scope.launch(ioDispatcher) {
            val trackMetadata = metadata.trackMetadata ?: return@launch
            val mbidMapping = trackMetadata.mbidMapping ?: return@launch
        }
//       will be implemented later after full SocialRepository migration
    }

}