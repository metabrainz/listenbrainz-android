package org.listenbrainz.android.model.albumSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumRelease(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("status")
    val status: String? = null
)