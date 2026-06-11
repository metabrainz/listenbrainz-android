package org.listenbrainz.shared.model.playback

import kotlinx.serialization.Serializable

@Serializable
data class SharedPlayerState(
    val trackUi: String?,
    val trackName: String?,
    val artistName:String?,
    val albumName:String?,
    val isPaused:Boolean,
    val playbackPosition:Long,
    val trackDuration:Long
)

@Serializable
data class SharedPlayerContext(
    val title: String?,
    val url:String
)