package org.listenbrainz.android.model.userPlaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPlaylistExtensionData(
    @SerialName("additional_metadata")
    val additionalMetadata: AdditionalMetadata = AdditionalMetadata(),
    @SerialName("collaborators")
    val collaborators: List<String> = listOf(),
    @SerialName("copied_from_deleted")
    val copiedFromDeleted: Boolean? = null,
    @SerialName("created_for")
    val createdFor: String? = null,
    @SerialName("creator")
    val creator: String? = null,
    @SerialName("last_modified_at")
    val lastModifiedAt: String? = null,
    @SerialName("public")
    val public: Boolean? = null
)