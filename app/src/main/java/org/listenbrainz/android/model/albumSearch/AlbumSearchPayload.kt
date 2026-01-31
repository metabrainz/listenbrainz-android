package org.listenbrainz.android.model.albumSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumSearchPayload(
    @SerialName("created")
    val created: String? = null,
    @SerialName("count")
    val count: Int? = null,
    @SerialName("offset")
    val offset: Int? = null,
    @SerialName("release-groups")
    val releaseGroups:List<AlbumData> = emptyList()
)