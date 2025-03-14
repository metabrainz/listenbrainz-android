package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ArtistCredit(
    @SerializedName("artist")
    val artist: RecordingArtist?,
    @SerializedName("joinphrase")
    val joinphrase: String?,
    @SerializedName("name")
    val name: String?
)