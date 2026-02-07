package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserFeedback(
    @SerialName("count") val count: Int? = null,
    @SerialName("feedback") val feedback: List<UserFeedbackEntry>? = null,
)
