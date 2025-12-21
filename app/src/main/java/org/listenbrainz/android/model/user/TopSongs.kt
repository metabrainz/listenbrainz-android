package org.listenbrainz.android.model.user

import kotlinx.serialization.Serializable

@Serializable
data class TopSongs(
    val payload: TopSongPayload? = TopSongPayload()
)