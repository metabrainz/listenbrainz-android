package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouPlaylists(
    @SerializedName("count")
    val count: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("playlist_count")
    val playlistCount: Int,
    @SerializedName("playlists")
    val playlists: List<Playlists>
)