package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val created: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val id: String? = null,
    val karma: Int? = null,
    @SerialName("musicbrainz_username") val musicbrainzUsername: String? = null,
    @SerialName("user_ref") val userRef: String? = null,
    @SerialName("user_type") val userType: String? = null
)