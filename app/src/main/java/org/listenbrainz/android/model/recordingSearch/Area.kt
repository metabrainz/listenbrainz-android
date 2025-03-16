package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class Area(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("iso-3166-1-codes")
    val iso31661Codes: List<String?> = emptyList(),
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("sort-name")
    val sortName: String? = null
)