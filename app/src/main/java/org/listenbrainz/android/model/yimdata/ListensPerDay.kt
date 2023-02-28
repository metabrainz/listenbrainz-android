package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class ListensPerDay (
    
    @SerializedName("from_ts"      ) var fromTs      : Int     = 0,
    @SerializedName("listen_count" ) var listenCount : Int     = 0,
    @SerializedName("time_range"   ) var timeRange   : String  = "",
    @SerializedName("to_ts"        ) var toTs        : Int     = 0

)