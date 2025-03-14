package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class Area(
    @SerializedName("id")
    val id: String?,
    @SerializedName("iso-3166-1-codes")
    val iso31661Codes: List<String?>?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("sort-name")
    val sortName: String?
)