package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class TopAlbumArtistInfo(
    @SerializedName("artist_credit_id") val artistCreditId: Int? = 0,
    val artists: List<ArtistPersonalInfo>? = listOf(),
    val name: String? = ""
)