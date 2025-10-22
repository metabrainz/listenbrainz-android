package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.MbidMapping
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist

data class Release(
    @SerializedName("artist_mbids")     val artistMbids: List<String>? = listOf(),
    @SerializedName("artist_name")      val artistName: String? = "",
    val artists: List<FeedListenArtist>? = listOf(),
    @SerializedName("caa_id")           val caaId: Long? = 0,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = "",
    @SerializedName("listen_count")     val listenCount: Int? = 0,
    @SerializedName("release_mbid")     val releaseMbid: String? = "",
    @SerializedName("release_name")     val releaseName: String? = ""
) {
    fun toMetadata() = Metadata(
        trackMetadata = TrackMetadata(
            artistName = artistName ?: "",
            trackName = releaseName ?: "",
            releaseName = releaseName,
            mbidMapping = if (releaseMbid != null || artistMbids?.isNotEmpty() == true) {
                MbidMapping(
                    artistMbids = artistMbids ?: emptyList(),
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