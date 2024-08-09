package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class Recording(
    @SerializedName("artist_mbids")     val artistMbids: List<String>? = listOf(),
    @SerializedName("artist_name")      val artistName: String? = "",
                                        val artists: List<TopArtistInfo>? = listOf(),
    @SerializedName("caa_id")           val caaId: Long? = 0,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = "",
    @SerializedName("listen_count")     val listenCount: Int? = 0,
    @SerializedName("recording_mbid")   val recordingMbid: String? = "",
    @SerializedName("release_mbid")     val releaseMbid: String? = "",
    @SerializedName("release_name")     val releaseName: String? = "",
    @SerializedName("track_name")       val trackName: String? = ""
)