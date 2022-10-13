package org.listenbrainz.android.data.sources.api.entities.acoustid

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Date {
    @SerializedName("day")
    @Expose
    var day = 0

    @SerializedName("month")
    @Expose
    var month = 0

    @SerializedName("year")
    @Expose
    var year = 0
}