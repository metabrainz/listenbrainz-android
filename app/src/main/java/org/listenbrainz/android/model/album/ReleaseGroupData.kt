package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseGroupData(
    val count: Int? = null,
    @SerialName("genre_mbid") val genreMbid: String? = null,
    val tag: String? = null
)