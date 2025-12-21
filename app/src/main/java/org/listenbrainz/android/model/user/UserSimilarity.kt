package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSimilarityPayload(
    @SerialName("payload") val userSimilarity: UserSimilarity = UserSimilarity()
)

@Serializable
data class UserSimilarity(
    @SerialName("similarity") val similarity: Float = 0f,
    @SerialName("user_name") val username: String = ""
)