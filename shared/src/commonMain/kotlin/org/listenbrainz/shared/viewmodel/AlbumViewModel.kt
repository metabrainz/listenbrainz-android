package org.listenbrainz.shared.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.listenbrainz.shared.repository.album.AlbumRepository
import org.listenbrainz.shared.ui.screens.album.AlbumUiState
import org.listenbrainz.shared.util.Utils

class AlbumViewModel(
    private val repository: AlbumRepository,
    private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel<AlbumUiState>() {
    private val albumUIStateFlow: MutableStateFlow<AlbumUiState> = MutableStateFlow(AlbumUiState())

    suspend fun fetchAlbumData(albumMbid: String?) {
        val albumInfo = repository.fetchAlbumInfo(albumMbid).data
        val albumData =  repository.fetchAlbum(albumMbid).data
        val albumReviews = repository.fetchAlbumReviews(albumMbid).data

        val albumUiState = AlbumUiState(
            isLoading = false,
            name = albumInfo?.title,
            coverArt = org.listenbrainz.shared.util.Utils.getCoverArtUrl(albumData?.caaReleaseMbid, albumData?.caaId),
            artists = albumData?.releaseGroupMetadata?.artist?.artists ?: listOf(),
            releaseDate = albumInfo?.firstReleaseDate,
            totalPlays = albumData?.listeningStats?.totalListenCount,
            totalListeners = albumData?.listeningStats?.totalUserCount,
            tags = albumData?.releaseGroupMetadata?.tag?.releaseGroup ?: listOf(),
            links = albumData?.releaseGroupMetadata?.artist?.artists?.get(0)?.rels,
            trackList = albumData?.mediums?.get(0)?.tracks ?: listOf(),
            topListeners = albumData?.listeningStats?.listeners ?: listOf(),
            reviews = albumReviews,
            type = albumData?.type
        )
        albumUIStateFlow.emit(albumUiState)
    }

    override val uiState: StateFlow<AlbumUiState> = createUiStateFlow()

    override fun createUiStateFlow(): StateFlow<AlbumUiState> {
        return combine(
            albumUIStateFlow
        ) {
            it[0]
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            AlbumUiState()
        )
    }
}