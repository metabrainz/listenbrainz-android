package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class User(
    val created: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    val id: String? = null,
    val karma: Int? = null,
    @SerializedName("musicbrainz_username") val musicbrainzUsername: String? = null,
    @SerializedName("user_ref") val userRef: String? = null,
    @SerializedName("user_type") val userType: String? = null
)