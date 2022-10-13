package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class ReleaseGroup {
    @SerializedName("artists")
    @Expose
    var artists: List<Artist> = ArrayList()

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("releases")
    @Expose
    var releases: List<Release> = ArrayList()

    @SerializedName("secondarytypes")
    @Expose
    var secondarytypes: List<String> = ArrayList()

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null
}