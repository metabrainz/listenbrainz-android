package org.listenbrainz.android.data.sources.api.entities.yimdata

import com.google.gson.annotations.SerializedName

data class Artists (
    
    @SerializedName("artist_mbid"  ) var artistMbid  : String  = "",
    @SerializedName("artist_name"  ) var artistName  : String  = "",
    @SerializedName("listen_count" ) var listenCount : Int     = 0

)