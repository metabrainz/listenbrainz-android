package org.listenbrainz.android.model.albumSearch

import org.listenbrainz.android.model.search.albumSearch.AlbumUiModel

fun AlbumData.toUiModel(): AlbumUiModel {
    val albumId = this.id
    val coverArtUrl = if (albumId != null) {
        "https://coverartarchive.org/release-group/$albumId/front-250"
    } else null
    return AlbumUiModel(
        id = this.id ?: "",
        title = this.title ?: "Unknown Album",
        type = this.primaryType ?: "Unknown Type",
        artistCredit = this.artistCredit,
        firstReleaseDate = this.firstReleaseDate ?: "",
        coverArtUrl = coverArtUrl
    )
}