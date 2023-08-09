package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class TrackMetadata(
    @SerializedName("additional_info") val additionalInfo: AdditionalInfo?,
    @SerializedName("artist_name"    ) val artistName: String,
    @SerializedName("mbid_mapping"   ) val mbidMapping: MbidMapping?,
    @SerializedName("release_name"   ) val releaseName: String?,
    @SerializedName("track_name"     ) val trackName: String
)