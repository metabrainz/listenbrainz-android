package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class UserPlaylistExtensionData(
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadata = AdditionalMetadata(),
    @SerializedName("collaborators")
    val collaborators: List<String> = listOf(),
    @SerializedName("copied_from_deleted")
    val copiedFromDeleted: Boolean? = null,
    @SerializedName("created_for")
    val createdFor: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("last_modified_at")
    val lastModifiedAt: String? = null,
    @SerializedName("public")
    val public: Boolean? = null
)