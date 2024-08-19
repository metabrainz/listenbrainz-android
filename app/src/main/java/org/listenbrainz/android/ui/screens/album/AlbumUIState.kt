package org.listenbrainz.android.ui.screens.album

import org.listenbrainz.android.model.album.ReleaseGroupData
import org.listenbrainz.android.model.album.Track
import org.listenbrainz.android.model.artist.Artist
import org.listenbrainz.android.model.artist.CBReview
import org.listenbrainz.android.model.artist.Listeners
import org.listenbrainz.android.model.artist.Rels

data class AlbumUiState(
    val isLoading: Boolean = true,
    val name: String? = null,
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