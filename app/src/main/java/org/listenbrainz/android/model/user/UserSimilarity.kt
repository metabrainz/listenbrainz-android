package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class UserSimilarityPayload(
    @SerializedName("payload") val userSimilarity: UserSimilarity
)

data class UserSimilarity(
    @SerializedName("similarity") val similarity: Float,
    @SerializedName("user_name") val username: String
)