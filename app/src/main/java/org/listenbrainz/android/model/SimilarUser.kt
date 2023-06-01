package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class SimilarUser(
    val similarity: Double,
    @SerializedName("user_name") val username: String
)