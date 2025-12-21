package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    @SerialName("caa_id") val caaId: Long? = null,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = null,
    @SerialName("listening_stats") val listeningStats: ListeningStats? = null,
    val mediums: List<Medium>? = listOf(),
    @SerialName("recordings_release_mbid") val recordingsReleaseMbid: String? = null,
    @SerialName("release_group_mbid") val releaseGroupMbid: String? = null,
    @SerialName("release_group_metadata") val releaseGroupMetadata: ReleaseGroupMetadata? = null,
    val type: String? = null
)