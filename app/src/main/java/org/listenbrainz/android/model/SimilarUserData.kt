package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class SimilarUserData(
    val payload: List<SimilarUser>? = null
)