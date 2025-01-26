package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class CopyPlaylistResponse(
    @SerializedName("playlist_mbid")
    val playlistMbid: String,
    @SerializedName("status")
    val status: String
)