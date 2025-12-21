package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23AdditionalMetadata (
    @SerialName("caa_id") var caaId: Long = 0,
    @SerialName("caa_release_mbid") var caaReleaseMbid: String = "",
)
