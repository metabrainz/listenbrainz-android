package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class ListenBrainzExternalServices(
    val services: List<String>,
    @SerializedName("user_name") val userName: String,
)