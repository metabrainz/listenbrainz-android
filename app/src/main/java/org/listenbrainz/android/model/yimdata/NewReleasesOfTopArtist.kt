package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewReleasesOfTopArtist (
    @SerialName("artist_credit_mbids") var artistCreditMbids: ArrayList<String> = arrayListOf(),
    @SerialName("artist_credit_name") var artistCreditName: String = "",
    @SerialName("caa_id") var caaId: Long = 0,
    @SerialName("caa_release_mbid") var caaReleaseMbid: String = "",
    @SerialName("release_group_mbid") var releaseGroupMbid: String = "",
    @SerialName("title") var title: String = ""
)