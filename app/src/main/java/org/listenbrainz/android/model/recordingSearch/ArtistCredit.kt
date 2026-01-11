package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistCredit(
    @SerialName("artist")
    val artist: RecordingArtist? = null,
    @SerialName("joinphrase")
    val joinphrase: String? = null,
    @SerialName("name")
    val name: String? = null
)