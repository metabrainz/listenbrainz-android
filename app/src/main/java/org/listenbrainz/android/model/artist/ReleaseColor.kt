package org.listenbrainz.android.model.artist

import kotlinx.serialization.Serializable

@Serializable
data class ReleaseColor(
    val blue: Int? = null,
    val green: Int? = null,
    val red: Int? = null
)