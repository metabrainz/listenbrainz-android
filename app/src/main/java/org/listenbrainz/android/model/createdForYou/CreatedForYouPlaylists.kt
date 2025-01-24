package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouPlaylists(
    @SerializedName("playlist")
    val playlist: CreatedForYouPlaylist = CreatedForYouPlaylist()
)