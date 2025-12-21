package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YimPayloadData (
    @SerialName("data")
    var data : YimData? = YimData(),      // null if a user is new, i.e., has 0 totalListenCount.
    @SerialName("user_name")
    var userName : String? = null
)