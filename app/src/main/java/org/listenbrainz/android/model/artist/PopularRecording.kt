package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class PopularRecording(
    @SerializedName("artist_mbids") val artistMbids: List<String>? = listOf(),
    @SerializedName("artist_name") val artistName: String? = "",
    val artists: List<ArtistXX>? = listOf(),
    @SerializedName("caa_id") val caaId: Long? = 0,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = "",
    val length: Int? = 0,
    @SerializedName("recording_mbid") val recordingMbid: String? = "",
    @SerializedName("recording_name") val recordingName: String? = "",
    @SerializedName("release_color") val releaseColor: ReleaseColor? = ReleaseColor(),
    @SerializedName("release_mbid") val releaseMbid: String? = "",
    @SerializedName("release_name") val releaseName: String? = "",
    @SerializedName("total_listen_count") val totalListenCount: Int? = 0,
    @SerializedName("total_user_count") val totalUserCount: Int? = 0
)