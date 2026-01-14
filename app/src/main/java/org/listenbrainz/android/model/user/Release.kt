package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.AdditionalInfo
import org.listenbrainz.shared.model.MbidMapping
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.shared.model.TrackMetadata
import org.listenbrainz.shared.model.feed.FeedListenArtist

@Serializable
data class Release(
    @SerialName("artist_mbids") val artistMbids: List<String> = emptyList(),
    @SerialName("artist_name") val artistName: String? = "",
    val artists: List<FeedListenArtist> = emptyList(),
    @SerialName("caa_id") val caaId: Long? = 0,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = "",
    @SerialName("listen_count") val listenCount: Int? = 0,
    @SerialName("release_mbid") val releaseMbid: String? = "",
    @SerialName("release_name") val releaseName: String? = ""
) {
    fun toMetadata() = Metadata(
        trackMetadata = TrackMetadata(
            artistName = artistName ?: "",
            trackName = releaseName ?: "",
            releaseName = releaseName,
            mbidMapping = if (releaseMbid != null || artistMbids.isNotEmpty()) {
                MbidMapping(
                    artistMbids = artistMbids,
                    artists = artists,
                    caaId = caaId,
                    caaReleaseMbid = caaReleaseMbid,
                    recordingMbid = null,
                    recordingName = releaseName ?: "",
                    releaseMbid = releaseMbid
                )
            } else null,
            additionalInfo = AdditionalInfo(
                artistMbids = artistMbids,
                releaseMbid = releaseMbid
            )
        )
    )
}