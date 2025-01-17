package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouPayload(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("offset")
    val offset: Int? = null,
    @SerializedName("playlist_count")
    val playlistCount: Int? = null,
    @SerializedName("playlists")
    val playlists: List<Playlists> = listOf()
)