package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class NewReleasesOfTopArtist (
    
    @SerializedName("artist_credit_mbids" ) var artistCreditMbids : ArrayList<String> = arrayListOf(),
    @SerializedName("artist_credit_name"  ) var artistCreditName  : String            = "",
    @SerializedName("caa_id"              ) var caaId             : Long              = 0,
    @SerializedName("caa_release_mbid"    ) var caaReleaseMbid    : String            = "",
    @SerializedName("release_group_mbid"  ) var releaseGroupMbid  : String            = "",
    @SerializedName("title"               ) var title             : String            = ""

)