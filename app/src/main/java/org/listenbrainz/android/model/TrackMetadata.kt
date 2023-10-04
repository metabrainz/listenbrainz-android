package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class TrackMetadata(
    @SerializedName("additional_info") val additionalInfo: AdditionalInfo?,
    @SerializedName("artist_name"    ) val artistName: String,
    @SerializedName("mbid_mapping"   ) val mbidMapping: MbidMapping?,
    @SerializedName("release_name"   ) val releaseName: String?,
    @SerializedName("track_name"     ) val trackName: String
)