package org.listenbrainz.shared.model.search.artistSearch

data class ArtistSearchUiState(
    val artists : List<ArtistUiModel> = emptyList()
)
