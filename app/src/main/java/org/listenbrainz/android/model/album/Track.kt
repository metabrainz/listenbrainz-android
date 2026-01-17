package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.feed.FeedListenArtist

@Serializable
data class Track(
    @SerialName("artist_mbids") val artistMbids: List<String?>? = null,
    val artists: List<FeedListenArtist?>? = null,
    val length: Int? = null,
    val name: String? = null,
    val position: Int? = null,
    @SerialName("recording_mbid") val recordingMbid: String? = null,
    @SerialName("total_listen_count") val totalListenCount: Int? = null,
    @SerialName("total_user_count") val totalUserCount: Int? = null
)