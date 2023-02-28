package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class YimPayloadData (
    @SerializedName("data")
    var data : YimData? = YimData(),      // null if a user is new, i.e., has 0 totalListenCount.
    @SerializedName("user_name")
    var userName : String? = null
)