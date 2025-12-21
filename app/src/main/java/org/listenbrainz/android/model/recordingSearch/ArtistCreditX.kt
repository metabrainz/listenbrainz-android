package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistCreditX(
    @SerialName("artist")
    val artist: ArtistX? = null,
    @SerialName("name")
    val name: String? = null
)