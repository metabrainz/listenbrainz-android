package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class TrackExtensionData(
    @SerializedName("added_at")
    val addedAt: String? = null,
    @SerializedName("added_by")
    val addedBy: String? = null,
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadataTrack = AdditionalMetadataTrack(),
    @SerializedName("artist_identifiers")
    val artistIdentifiers: List<String> = listOf()
)