package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class TopArtist (
    @SerializedName("artist_mbids" ) var artistMbids : ArrayList<String>? = arrayListOf(),  // Can be empty
    @SerializedName("artist_name"  ) var artistName  : String?           = null,
    @SerializedName("listen_count" ) var listenCount : Int?              = null
)