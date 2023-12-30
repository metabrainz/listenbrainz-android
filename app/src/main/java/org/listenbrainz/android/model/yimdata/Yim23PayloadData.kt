package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23PayloadData (
    @SerializedName("data")
    var data : Yim23Data? = Yim23Data(),      // null if a user is new, i.e., has 0 totalListenCount.
    @SerializedName("user_name")
    var userName : String? = null
)