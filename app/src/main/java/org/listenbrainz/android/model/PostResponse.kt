package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Responses which return:
 * ```
 * {status: "ok"}
 * ```
 * must have this data class as their response.*/
@Serializable
data class PostResponse(
    @SerialName("status") val status: String? = null
)
// TODO: Update with error responses after feed section phase 1.
