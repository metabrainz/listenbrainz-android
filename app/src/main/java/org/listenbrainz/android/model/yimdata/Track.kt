package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Track (
    @SerializedName("creator"    ) var creator    : String    = "",
    @SerializedName("identifier" ) var identifier : String    = "",
    @SerializedName("title"      ) var title      : String    = ""
)
