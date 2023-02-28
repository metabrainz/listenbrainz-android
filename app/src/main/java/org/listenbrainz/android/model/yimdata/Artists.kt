package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Artists (
    
    @SerializedName("artist_mbid"  ) var artistMbid  : String  = "",
    @SerializedName("artist_name"  ) var artistName  : String  = "",
    @SerializedName("listen_count" ) var listenCount : Int     = 0

)