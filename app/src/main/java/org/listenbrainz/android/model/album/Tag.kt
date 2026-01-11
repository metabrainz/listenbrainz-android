package org.listenbrainz.android.model.album

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val count: Int? = null,
    val name: String? = null
)