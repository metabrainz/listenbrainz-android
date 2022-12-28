package org.listenbrainz.android.data.sources.api.entities.yimdata

import com.google.gson.annotations.SerializedName

data class TopRelease (
    
    @SerializedName("artist_mbids"     ) var artistMbids    : ArrayList<String> = arrayListOf(),
    @SerializedName("artist_name"      ) var artistName     : String?           = null,
    @SerializedName("listen_count"     ) var listenCount    : Int?              = null,
    @SerializedName("release_name"     ) var releaseName    : String?           = null,
    // Temporary fields
    @SerializedName("caa_id"           ) var caaId          : Int?              = null,   // May or may not be present in api call.
    @SerializedName("caa_release_mbid" ) var caaReleaseMbid : String?           = null,
    @SerializedName("release_mbid"     ) var releaseMbid    : String?           = null

)