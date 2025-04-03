package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class PlaylistExtensionData(
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadata = AdditionalMetadata(),
    @SerializedName("created_for")
    val createdFor: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("last_modified_at")
    val lastModifiedAt: String? = null,
    @SerializedName("public")
    val `public`: Boolean? = null,
    @SerializedName("collaborators")
    val collaborators: List<String> = emptyList(),
)