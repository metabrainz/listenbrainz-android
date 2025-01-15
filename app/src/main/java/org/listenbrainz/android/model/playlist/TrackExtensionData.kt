package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class TrackExtensionData(
    @SerializedName("added_at")
    val addedAt: String,
    @SerializedName("added_by")
    val addedBy: String,
    @SerializedName("additional_metadata")
    val additionalMetadata: AdditionalMetadataTrack,
    @SerializedName("artist_identifiers")
    val artistIdentifiers: List<String>
)