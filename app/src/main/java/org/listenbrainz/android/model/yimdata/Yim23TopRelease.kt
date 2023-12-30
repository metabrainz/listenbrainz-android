package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class TopReleaseYim23 (

    @SerializedName("artist_mbids"     ) var artistMbids    : ArrayList<String> = arrayListOf(),
    @SerializedName("artist_name"      ) var artistName     : String            = "",
    @SerializedName("listen_count"     ) var listenCount    : Int               = 0,
    @SerializedName("release_group_name"     ) var releaseName    : String            = "",
    // Temporary fields
    @SerializedName("caa_id"           ) var caaId          : Long?             = null,   // May or may not be present in api call.
    @SerializedName("caa_release_mbid" ) var caaReleaseMbid : String?           = null,
    @SerializedName("release_group_mbid"     ) var releaseMbid    : String?           = null

)