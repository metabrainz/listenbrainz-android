package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class Playlists(
    @SerializedName("playlist")
    val playlist: Playlist
)