package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.artist.ArtistWithTags

data class AlbumTags(
    val artist: List<ArtistWithTags>? = listOf(),
    @SerializedName("release_group") val releaseGroup: List<ReleaseGroupData>? = listOf()
)