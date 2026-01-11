package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist

@Serializable
data class Recording(
    @SerialName("artist_mbids") val artistMbids: List<String>? = listOf(),
    @SerialName("artist_name") val artistName: String? = "",
    val artists: List<FeedListenArtist>? = listOf(),
    @SerialName("caa_id") val caaId: Long? = 0,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = "",
    @SerialName("listen_count") val listenCount: Int? = 0,
    @SerialName("recording_mbid") val recordingMbid: String? = "",
    @SerialName("release_mbid") val releaseMbid: String? = "",
    @SerialName("release_name") val releaseName: String? = "",
    @SerialName("track_name") val trackName: String? = ""
) {
    fun toTrackMetadata(): TrackMetadata =
        TrackMetadata(
            artistName = artistName ?: "",
            releaseName = releaseName,
            trackName = trackName ?: "",
            mbidMapping = MbidMapping(
                artistMbids = artistMbids ?: listOf(),
                recordingName = releaseName ?: "",
                caaId = caaId,
                caaReleaseMbid = caaReleaseMbid,
                recordingMbid = recordingMbid
            ),
            additionalInfo = null
        )
}