package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.feed.FeedListenArtist

data class PopularRecording(
    @SerializedName("artist_mbids") val artistMbids: List<String>? = listOf(),
    @SerializedName("artist_name") val artistName: String? = null,
    val artists: List<FeedListenArtist>? = listOf(),
    @SerializedName("caa_id") val caaId: Long? = null,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val length: Int? = null,
    @SerializedName("recording_mbid") val recordingMbid: String? = null,
    @SerializedName("recording_name") val recordingName: String? = null,
    @SerializedName("release_color") val releaseColor: ReleaseColor? = ReleaseColor(),
    @SerializedName("release_mbid") val releaseMbid: String? = null,
    @SerializedName("release_name") val releaseName: String? = null,
    @SerializedName("total_listen_count") val totalListenCount: Int? = null,
    @SerializedName("total_user_count") val totalUserCount: Int? = null
)