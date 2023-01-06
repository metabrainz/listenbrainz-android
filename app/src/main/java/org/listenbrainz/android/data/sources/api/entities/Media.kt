package org.listenbrainz.android.data.sources.api.entities

import com.google.gson.annotations.SerializedName
import java.util.*

class Media {
    @SerializedName("track-count")
    var trackCount = 0
    var format: String? = null
    var title: String? = null
    var position = 0
    var tracks: List<Track> = ArrayList()
}