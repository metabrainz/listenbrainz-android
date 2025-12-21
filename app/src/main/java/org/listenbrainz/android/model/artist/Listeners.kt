package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Listeners(
    @SerialName("listen_count") val listenCount: Int? = null,
    @SerialName("user_name") val userName: String? = null
)