package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class SimilarUserPayload(
    val similarity: Double,
    @SerializedName("user_name") val userName: String
)