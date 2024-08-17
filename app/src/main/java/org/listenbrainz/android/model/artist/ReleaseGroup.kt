package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ReleaseGroup(
    @SerializedName("artist_credit_name") val artistCreditName: String? = "",
    val artists: List<ArtistXX>? = listOf(),
    @SerializedName("caa_id") val caaId: Long? = 0,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = "",
    val date: String? = "",
    val mbid: String? = "",
    val name: String? = "",
    @SerializedName("total_listen_count") val totalListenCount: Int? = 0,
    @SerializedName("total_user_count") val totalUserCount: Int? = 0,
    val type: String? = ""
)