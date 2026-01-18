package org.listenbrainz.android.model.artistSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistTag(
    @SerialName("name")
    val name:String?=null,
    @SerialName("count")
    val count:Int? = null
)
