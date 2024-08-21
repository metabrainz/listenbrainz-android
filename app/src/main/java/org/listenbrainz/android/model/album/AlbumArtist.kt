package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.artist.Artist

data class AlbumArtist(
    @SerializedName("artist_credit_id") val artistCreditId: Int? = null,
    val artists: List<Artist>? = listOf(),
    val name: String? = null,
)