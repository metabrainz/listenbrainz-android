package org.listenbrainz.android.model.artistSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistLifeSpan(
    @SerialName("begin")
    val begin:String? =null,
    @SerialName("end")
    val end:String? =null,
    @SerialName("ended")
    val ended:Boolean = false
)
