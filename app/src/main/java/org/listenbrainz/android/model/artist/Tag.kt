package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Tag(
    val artist: List<ArtistTagsInfo>? = listOf(),
    @SerializedName("release_group") val releaseGroup: List<AlbumTagInfo>? = listOf()
)