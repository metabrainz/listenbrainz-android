package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class Aliase(
    @SerializedName("begin-date")
    val beginDate: String? = null,
    @SerializedName("end-date")
    val endDate: String? = null,
    @SerializedName("locale")
    val locale: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("primary")
    val primary: Any? = null,
    @SerializedName("sort-name")
    val sortName: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("type-id")
    val typeId: String? = null
)