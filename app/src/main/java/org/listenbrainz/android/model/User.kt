package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_name") val userName: String
)