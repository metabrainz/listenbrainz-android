package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class UserFeedback(
    @SerializedName("count")    val count: Int? = null,
    @SerializedName("feedback") val feedback: List<UserFeedbackEntry>? = null,
)
