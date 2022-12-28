package org.listenbrainz.android.data.sources.api.entities.yimdata

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.yimdata.Data

data class Payload (
    @SerializedName("data")
    var data : Data? = Data(),      // null if a user is new, i.e., has 0 totalListenCount.
    @SerializedName("user_name")
    var userName : String? = null
)