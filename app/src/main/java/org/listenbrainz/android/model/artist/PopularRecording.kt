package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.feed.FeedListenArtist

@Serializable
data class PopularRecording(
    @SerialName("artist_mbids") val artistMbids: List<String>? = listOf(),
    @SerialName("artist_name") val artistName: String? = null,
    val artists: List<FeedListenArtist> = emptyList(),
    @SerialName("caa_id") val caaId: Long? = null,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val length: Int? = null,
    @SerialName("recording_mbid") val recordingMbid: String? = null,
    @SerialName("recording_name") val recordingName: String? = null,
    @SerialName("release_color") val releaseColor: ReleaseColor? = ReleaseColor(),
    @SerialName("release_mbid") val releaseMbid: String? = null,
    @SerialName("release_name") val releaseName: String? = null,
    @SerialName("total_listen_count") val totalListenCount: Int? = null,
    @SerialName("total_user_count") val totalUserCount: Int? = null
)