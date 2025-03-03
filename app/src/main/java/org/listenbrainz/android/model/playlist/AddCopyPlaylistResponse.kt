package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class AddCopyPlaylistResponse(
    @SerializedName("playlist_mbid")
    val playlistMbid: String,
    @SerializedName("status")
    val status: String
)

data class EditPlaylistResponse(
    @SerializedName("status")
    val status: String
)