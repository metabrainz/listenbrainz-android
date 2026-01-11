package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistExtensionData(
    @SerialName("additional_metadata")
    val additionalMetadata: AdditionalMetadata = AdditionalMetadata(),
    @SerialName("created_for")
    val createdFor: String? = null,
    @SerialName("creator")
    val creator: String? = null,
    @SerialName("last_modified_at")
    val lastModifiedAt: String? = null,
    @SerialName("public")
    val `public`: Boolean? = null,
    @SerialName("collaborators")
    val collaborators: List<String> = emptyList(),
)