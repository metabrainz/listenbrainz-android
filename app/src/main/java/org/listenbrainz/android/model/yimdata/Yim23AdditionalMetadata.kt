package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23AdditionalMetadata (
    @SerializedName("caa_id") var caaId    : Long   = 0,
    @SerializedName("caa_release_mbid") var caaReleaseMbid   : String   = "",

    )
