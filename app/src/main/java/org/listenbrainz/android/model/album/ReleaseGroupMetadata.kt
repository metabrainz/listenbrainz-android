package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName

data class ReleaseGroupMetadata(
    val artist: AlbumArtist? = null,
    val release: Release? = null,
    @SerializedName("release_group") val releaseGroup: Release? = null,
    val tag: AlbumTags? = null,
)