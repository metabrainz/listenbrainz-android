package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23Track (
    @SerializedName("creator"    ) var creator    : String    = "",
    @SerializedName("identifier" ) var identifier : String    = "",
    @SerializedName("extension") var extension : Yim23TrackExtension = Yim23TrackExtension(),
    @SerializedName("title"      ) var title      : String    = ""
)
