package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(
    @SerialName("1200") val size1200: String? = null,
    @SerialName("250") val size250: String? = null,
    @SerialName("500") val size500: String? = null,
    val large: String? = null,
    val small: String? = null
)