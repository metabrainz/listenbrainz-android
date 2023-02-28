package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

class Url {
    @SerializedName("id")
    var mbid: String? = null
    var resource: String? = null
}