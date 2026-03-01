package org.listenbrainz.android.model.search.albumSearch

import org.listenbrainz.android.model.recordingSearch.ArtistCredit

data class AlbumUiModel(
    val id:String,
    val title:String,
    val type:String,
    val artistCredit: List<ArtistCredit>,
    val firstReleaseDate: String,
    val coverArtUrl : String? = null
)
