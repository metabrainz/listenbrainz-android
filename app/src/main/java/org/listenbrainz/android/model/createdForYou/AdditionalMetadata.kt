package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class AdditionalMetadata(
    @SerializedName("algorithm_metadata")
    val algorithmMetadata: AlgorithmMetadata,
    @SerializedName("expires_at")
    val expiresAt: String
)