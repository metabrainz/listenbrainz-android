package org.listenbrainz.android.model.albumSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumTag(
    @SerialName("name")
    val name:String?=null,
    @SerialName("count")
    val count:Int? = null
)
