package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Track {
    @SerializedName("artists")
    @Expose
    var artists: List<Artist> = ArrayList()

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("position")
    @Expose
    var position = 0
}