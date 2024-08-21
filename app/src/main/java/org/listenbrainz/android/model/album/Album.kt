package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("caa_id") val caaId: Long? = null,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = null,
    @SerializedName("listening_stats") val listeningStats: ListeningStats? = null,
    val mediums: List<Medium>? = listOf(),
    @SerializedName("recordings_release_mbid") val recordingsReleaseMbid: String? = null,
    @SerializedName("release_group_mbid") val releaseGroupMbid: String? = null,
    @SerializedName("release_group_metadata")val releaseGroupMetadata: ReleaseGroupMetadata? = null,
    val type: String? = null
)