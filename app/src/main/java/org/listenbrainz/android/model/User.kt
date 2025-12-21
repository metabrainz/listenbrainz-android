package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_name") val username: String = "",
    @SerialName("is_followed") val isFollowed: Boolean = false
)