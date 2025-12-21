package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.artist.Artist

@Serializable
data class AlbumArtist(
    @SerialName("artist_credit_id") val artistCreditId: Int? = null,
    val artists: List<Artist>? = listOf(),
    val name: String? = null,
)