package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

/** Responses which return:
 * ```
 * {status: "ok"}
 * ```
 * must have this data class as their response.*/
data class PostResponse(
    @SerializedName("status") val status: String
)
// TODO: Update with error responses after feed section phase 1.
