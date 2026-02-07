package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.artist.ArtistWithTags

@Serializable
data class AlbumTags(
    val artist: List<ArtistWithTags?>? = listOf(),
    @SerialName("release_group") val releaseGroup: List<ReleaseGroupData?> = listOf()
)