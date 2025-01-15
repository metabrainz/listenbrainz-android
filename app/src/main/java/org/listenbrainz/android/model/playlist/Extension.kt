package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class Extension(
    @SerializedName("https://musicbrainz.org/doc/jspf#playlist")
    val playlistExtensionData: PlaylistExtensionData
)