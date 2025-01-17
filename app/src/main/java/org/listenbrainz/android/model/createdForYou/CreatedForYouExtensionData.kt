package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouExtensionData(
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadata,
    @SerializedName("created_for")
    val createdFor: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("last_modified_at")
    val lastModifiedAt: String,
    @SerializedName("public")
    val `public`: Boolean
)