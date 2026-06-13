package org.listenbrainz.shared.model.search.albumSearch

data class AlbumSearchUiState(
    val albums : List<AlbumUiModel> = emptyList()
)
