package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class PlaylistPayload(
    @SerializedName("playlist")
    val playlist: Playlist
)