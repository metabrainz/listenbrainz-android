package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23TopDiscoveriesPlaylist (
    @SerializedName("creator"    ) var creator    : String    = "",
    @SerializedName("identifier" ) var identifier : String    = "",
    @SerializedName("track"      ) var tracks      : List<Yim23Track>    = listOf()
)
