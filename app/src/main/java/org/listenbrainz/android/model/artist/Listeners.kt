package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Listeners(
    @SerializedName("listen_count") val listenCount: Int? = null,
    @SerializedName("user_name") val userName: String? = null
)