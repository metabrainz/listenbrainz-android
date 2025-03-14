package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingReleaseGroup(
    @SerializedName("id")
    val id: String?,
    @SerializedName("primary-type")
    val primaryType: String?,
    @SerializedName("primary-type-id")
    val primaryTypeId: String?,
    @SerializedName("secondary-type-ids")
    val secondaryTypeIds: List<String?>?,
    @SerializedName("secondary-types")
    val secondaryTypes: List<String?>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("type-id")
    val typeId: String?
)