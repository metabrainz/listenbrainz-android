package org.listenbrainz.android.viewmodel

import android.net.Uri
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
import org.listenbrainz.android.R
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.android.model.RecommendationData
import org.listenbrainz.android.model.RecommendationMetadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Review
import org.listenbrainz.android.model.ReviewMetadata
import org.listenbrainz.android.model.SocialData
import org.listenbrainz.shared.model.TrackMetadata
import org.listenbrainz.android.repository.listens.ListensRepository
import org.listenbrainz.shared.repository.AppPreferences
import org.listenbrainz.android.repository.remoteplayer.RemotePlaybackHandler
import org.listenbrainz.android.repository.social.SocialRepository
import org.listenbrainz.android.util.Resource
import org.listenbrainz.shared.model.ReviewEntityType
import org.listenbrainz.shared.social.SocialStateHolder
import org.listenbrainz.shared.social.SocialUiState

class SocialViewModel(
    private val socialStateHolder: SocialStateHolder,
    private val remotePlaybackHandler: RemotePlaybackHandler,
    private val ioDispatcher: CoroutineDispatcher,
    repository: SocialRepository
): FollowUnfollowModel<SocialUiState>(repository, ioDispatcher) {

    override val uiState: StateFlow<SocialUiState> = socialStateHolder.uiState

    override fun createUiStateFlow(): StateFlow<SocialUiState> = socialStateHolder.uiState

    fun searchUser(query: String){
        socialStateHolder.searchUser(query)
    }

    suspend fun isCritiqueBrainzLinked(): Boolean? {
        return socialStateHolder.isCritiqueBrainzLinked()
    }

    fun recommend(metadata: Metadata){
        socialStateHolder.recommend(metadata)
    }

    fun personallyRecommend(metadata: Metadata, users: List<String>, blurbContent: String) {
        socialStateHolder.personallyRecommend(metadata, users, blurbContent)
    }

    fun review(metadata: Metadata, entityType: ReviewEntityType, blurbContent: String, rating: Int?, locale: String) {
        socialStateHolder.review(metadata, blurbContent, rating, locale, entityType)
    }

    fun pin(metadata: Metadata, blurbContent: String?) {
        socialStateHolder.pin(metadata, blurbContent)
    }

    suspend fun getFollowers() = socialStateHolder.getFollowers()

    fun playListen(trackMetadata: TrackMetadata) {
        val spotifyId = trackMetadata.additionalInfo?.spotifyId
        if (spotifyId != null) {
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

    fun play(){
        remotePlaybackHandler.play()
    }

    fun pause(){
        remotePlaybackHandler.pause()
    }
}