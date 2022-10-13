package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Release {
    @SerializedName("country")
    @Expose
    var country: String? = null

    @SerializedName("date")
    @Expose
    var date: Date? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("medium_count")
    @Expose
    var mediumCount = 0

    @SerializedName("mediums")
    @Expose
    var mediums: List<Medium> = ArrayList()

    @SerializedName("releaseEvents")
    @Expose
    var releaseEvents: List<ReleaseEvent> = ArrayList()

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("track_count")
    @Expose
    var trackCount = 0
}