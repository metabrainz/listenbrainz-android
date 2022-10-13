package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Medium {
    @SerializedName("format")
    @Expose
    var format: String? = null

    @SerializedName("position")
    @Expose
    var position = 0

    @SerializedName("track_count")
    @Expose
    var trackCount = 0

    @SerializedName("tracks")
    @Expose
    var tracks: List<Track> = ArrayList()
}