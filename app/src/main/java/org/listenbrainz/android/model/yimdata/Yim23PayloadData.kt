package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23PayloadData (
    @SerialName("data")
    var data : Yim23Data? = Yim23Data(),      // null if a user is new, i.e., has 0 totalListenCount.
    @SerialName("user_name")
    var userName : String? = null
)