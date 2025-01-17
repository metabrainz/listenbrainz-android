package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class AlgorithmMetadata(
    @SerializedName("source_patch")
    val sourcePatch: String
)