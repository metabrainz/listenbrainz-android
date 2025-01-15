package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class TrackExtension(
    @SerializedName("https://musicbrainz.org/doc/jspf#track")
    val trackExtensionData: TrackExtensionData
)