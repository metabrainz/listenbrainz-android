package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingReleaseGroup(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("primary-type")
    val primaryType: String? = null,
    @SerializedName("primary-type-id")
    val primaryTypeId: String? = null,
    @SerializedName("secondary-type-ids")
    val secondaryTypeIds: List<String?> = emptyList(),
    @SerializedName("secondary-types")
    val secondaryTypes: List<String?> = emptyList(),
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("type-id")
    val typeId: String? = null
)