package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Recording {
    @SerializedName("artists")
    @Expose
    var artists: List<Artist> = ArrayList()

    @SerializedName("duration")
    @Expose
    var duration = 0

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("releasegroups")
    @Expose
    var releaseGroups: List<ReleaseGroup> = ArrayList()

    @SerializedName("title")
    @Expose
    var title: String? = null
    var sources = 0
}