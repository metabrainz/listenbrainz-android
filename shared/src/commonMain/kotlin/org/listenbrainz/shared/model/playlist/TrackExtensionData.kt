package org.listenbrainz.shared.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackExtensionData(
    @SerialName("added_at")
    val addedAt: String? = null,
    @SerialName("added_by")
    val addedBy: String? = null,
    @SerialName("additional_metadata")
    val additionalMetadata: AdditionalMetadataTrack = AdditionalMetadataTrack(),
    @SerialName("artist_identifiers")
    val artistIdentifiers: List<String> = listOf()
)