package org.listenbrainz.android.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.listenbrainz.android.repository.artist.ArtistRepository
import org.listenbrainz.android.ui.screens.artist.ArtistUIState
import org.listenbrainz.android.util.LinkUtils.parseLinks

class ArtistViewModel(
    private val repository: ArtistRepository,
    private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<ArtistUIState>() {
    private val artistUIStateFlow: MutableStateFlow<ArtistUIState> = MutableStateFlow(ArtistUIState())

    suspend fun fetchArtistData(artistMbid: String?) {
        val artistData = repository.fetchArtistData(artistMbid).data
        val artistReviews = repository.fetchArtistReviews(artistMbid).data
        val artistWikiExtract = repository.fetchArtistWikiExtract(artistMbid).data
        val appearsOn = artistData?.releaseGroups?.filter { releaseGroup ->
            releaseGroup?.artists?.get(0)?.artistMbid != artistMbid
        }
        val linksMap = parseLinks(artistMbid, artistData?.artist?.rels)
        val artistUiState = ArtistUIState(
            isLoading = false,
            name = artistData?.artist?.name,
            coverArt = artistData?.coverArt,
            beginYear = artistData?.artist?.beginYear,
            area = artistData?.artist?.area,
            totalPlays = artistData?.listeningStats?.totalListenCount,
            totalListeners = artistData?.listeningStats?.totalUserCount,
            wikiExtract = artistWikiExtract,
            tags = artistData?.artist?.tag,
            links = artistData?.artist?.rels,
            linksMap = linksMap,
            popularTracks = artistData?.popularRecordings,
            albums = artistData?.releaseGroups,
            appearsOn = appearsOn,
            similarArtists = artistData?.similarArtists?.artists,
            topListeners = artistData?.listeningStats?.listeners,
            reviews = artistReviews,
            artistMbid = artistMbid
        )
        artistUIStateFlow.emit(artistUiState)
    }


    override val uiState: StateFlow<ArtistUIState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<ArtistUIState> {
        return combine(
            artistUIStateFlow
        ) {
            it[0]
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            ArtistUIState()
        )
    }
}