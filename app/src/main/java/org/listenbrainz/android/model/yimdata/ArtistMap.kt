package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName


data class ArtistMap (

    @SerializedName("artist_count" ) var artistCount : Int                = 0,
    @SerializedName("artists"      ) var artists     : ArrayList<Artists> = arrayListOf(),
    @SerializedName("country"      ) var country     : String?            = null,
    @SerializedName("listen_count" ) var listenCount : Int                = 0

)
