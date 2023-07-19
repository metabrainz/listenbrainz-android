package org.listenbrainz.android.model

/** Responses which return:
 * ```
 * {status: "ok"}
 * ```
 * must have this data class as their response.*/
data class PostResponse(
    val status: String? = null
)
// TODO: Update with error responses after feed section phase 1.
