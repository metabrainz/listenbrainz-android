package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.feed.FeedListenArtist

@Immutable
@Serializable
data class MbidMapping(
    @SerialName("artist_mbids")
    val artistMbids: List<String> = emptyList(),
    @SerialName("artists")
    val artists: List<FeedListenArtist> = emptyList(),
    @SerialName("caa_id")
    val caaId: Long? = null,
    @SerialName("caa_release_mbid")
    val caaReleaseMbid: String? = null,
    @SerialName("recording_mbid")
    val recordingMbid: String? = null,
    @SerialName("recording_name")
    val recordingName: String? = null,
    @SerialName("release_mbid")
    val releaseMbid: String? = null
)