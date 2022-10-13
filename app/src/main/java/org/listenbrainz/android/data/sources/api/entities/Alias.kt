package org.listenbrainz.android.data.sources.api.entities

import com.google.gson.annotations.SerializedName

class Alias {
    @SerializedName("sort-name")
    var sortName: String? = null
    var name: String? = null
    var locale: String? = null
    var type: String? = null
    var isPrimary = false

    @SerializedName("begin-date")
    var beginDate: String? = null

    @SerializedName("end-date")
    var endDate: String? = null
    override fun toString(): String {
        return "Alias{" +
                "sortName='" + sortName + '\'' +
                ", name='" + name + '\'' +
                ", locale='" + locale + '\'' +
                ", type='" + type + '\'' +
                ", primary=" + isPrimary +
                ", beginDate='" + beginDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}'
    }
}