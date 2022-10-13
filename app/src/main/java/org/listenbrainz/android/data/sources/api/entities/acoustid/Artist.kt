package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Artist {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("joinphrase")
    @Expose
    var joinphrase: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null
}