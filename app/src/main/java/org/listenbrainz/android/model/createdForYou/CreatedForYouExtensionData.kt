package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouExtensionData(
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadata = AdditionalMetadata(),
    @SerializedName("created_for")
    val createdFor: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("last_modified_at")
    val lastModifiedAt: String? = null,
    @SerializedName("public")
    val public: Boolean? = null
)