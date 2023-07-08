package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_name") val username: String,
    @SerializedName("is_followed") val isFollowed: Boolean = false
)