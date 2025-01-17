package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class AlgorithmMetadata(
    @SerializedName("source_patch")
    val sourcePatch: String? = null
)