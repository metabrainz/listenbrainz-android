package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("metabrainz_user_id")
    val userId: String? = null,
    val profile: String? = null,
    @SerialName("sub")
    val username: String? = null
)