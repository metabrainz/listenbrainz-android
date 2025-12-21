package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseGroupMetadata(
    val artist: AlbumArtist? = null,
    val release: Release? = null,
    @SerialName("release_group") val releaseGroup: Release? = null,
    val tag: AlbumTags? = null,
)