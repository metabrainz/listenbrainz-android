package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class AcoustIDResponse {
    @SerializedName("results")
    @Expose
    var results: List<Result> = ArrayList()

    @SerializedName("status")
    @Expose
    var status: String? = null
}