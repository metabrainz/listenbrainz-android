package org.listenbrainz.android.ui.screens.album

import org.listenbrainz.shared.model.album.ReleaseGroupData
import org.listenbrainz.shared.model.album.Track
import org.listenbrainz.shared.model.artist.Artist
import org.listenbrainz.shared.model.artist.CBReview
import org.listenbrainz.shared.model.artist.Listeners
import org.listenbrainz.shared.model.artist.Rels

data class AlbumUiState(
    val isLoading: Boolean = true,
    val name: String? = null,
    val coverArt: String? = null,
    val artists: List<Artist?> = listOf(),
    val releaseDate: String? = null,
    val totalPlays: Int? = null,
    val totalListeners: Int? = null,
    val tags: List<ReleaseGroupData?> = listOf(),
    val links: Rels? = null,
    val trackList: List<Track?> = listOf(),
    val topListeners: List<Listeners?> = listOf(),
    val reviews: CBReview? = null,
    val type: String? = null,
)