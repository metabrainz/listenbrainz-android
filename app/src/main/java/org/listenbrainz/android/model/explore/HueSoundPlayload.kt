package org.listenbrainz.android.model.explore

import com.google.gson.annotations.SerializedName

data class HueSoundPayload(
    val payload: HueSoundPayloadData
)

data class HueSoundPayloadData (
    val releases : List<Release>
)

data class Release(
    @SerializedName("artist_name")val artistName: String,
    @SerializedName("release_name")val releaseName: String,
    @SerializedName("release_mbid")val releaseId: String,
    @SerializedName("caa_id")val caaId: String
)
