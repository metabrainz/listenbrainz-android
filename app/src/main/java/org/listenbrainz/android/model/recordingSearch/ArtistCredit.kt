package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ArtistCredit(
    @SerializedName("artist")
    val artist: RecordingArtist? = null,
    @SerializedName("joinphrase")
    val joinphrase: String? = null,
    @SerializedName("name")
    val name: String? = null
)