package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError (
    val code: Int? = null,
    val error: String? = null
)