package org.listenbrainz.android.ui.screens.artist

import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.model.artist.ArtistWikiExtract
import org.listenbrainz.android.model.artist.Listeners
import org.listenbrainz.android.model.artist.PopularRecording
import org.listenbrainz.android.model.artist.ReleaseGroup
import org.listenbrainz.android.model.artist.Rels
import org.listenbrainz.android.model.artist.SimilarArtist
import org.listenbrainz.android.model.artist.Tag

data class ArtistUIState(
    val isLoading: Boolean = true,
    val name: String? = null,
    val coverArt: String? = null,
    val beginYear: Int? = null,
    val area: String? = null,
    val totalPlays: Int? = null,
    val totalListeners: Int? = null,
    val wikiExtract: ArtistWikiExtract? = null,
    val tags: Tag? = null,
    val links: Rels? = null,
    val popularTracks: List<PopularRecording?>? = listOf(),
    val albums: List<ReleaseGroup?>? = listOf(),
    val appearsOn: List<ReleaseGroup?>? = listOf(),
    val similarArtists: List<SimilarArtist?>? = listOf(),
    val topListeners: List<Listeners?>? = listOf(),
    val reviews: CBReview? = null,
    val artistMbid: String? = null
)