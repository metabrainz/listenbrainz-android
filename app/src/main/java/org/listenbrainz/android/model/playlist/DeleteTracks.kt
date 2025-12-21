package org.listenbrainz.android.model.playlist

import kotlinx.serialization.Serializable

@Serializable
data class DeleteTracks(
    val index: Int = 0,
    val count: Int = 0
)
