package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenWorkerInput(
    val track: PlayingTrack,
    val listenType: ListenType
)
