package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ArtistCreditX(
    @SerializedName("artist")
    val artist: ArtistX? = null,
    @SerializedName("name")
    val name: String? = null
)