package org.listenbrainz.shared.model.playback

import kotlinx.serialization.Serializable

@Serializable
data class SharedPlayerState(
    val trackUri: String?,
    val trackName: String?,
    val artistName:String?,
    val imageUri: String?,
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