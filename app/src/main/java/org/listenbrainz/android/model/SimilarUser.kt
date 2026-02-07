package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimilarUser(
    val similarity: Double = 0.0,
    @SerialName("user_name") val username: String = ""
)