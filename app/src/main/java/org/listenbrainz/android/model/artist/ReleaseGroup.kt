package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.shared.model.feed.FeedListenArtist

@Serializable
data class ReleaseGroup(
    @SerialName("artist_credit_name") val artistCreditName: String? = null,
    val artists: List<FeedListenArtist>? = listOf(),
    @SerialName("caa_id") val caaId: Long? = null,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val date: String? = null,
    val mbid: String? = null,
    val name: String? = null,
    @SerialName("total_listen_count") val totalListenCount: Int? = null,
    @SerialName("total_user_count") val totalUserCount: Int? = null,
    val type: String? = null
)