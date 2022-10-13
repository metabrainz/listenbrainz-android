package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Result {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("recordings")
    @Expose
    var recordings: List<Recording> = ArrayList()

    @SerializedName("score")
    @Expose
    var score = 0.0
}