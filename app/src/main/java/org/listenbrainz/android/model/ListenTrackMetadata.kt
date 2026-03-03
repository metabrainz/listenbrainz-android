package org.listenbrainz.android.model

import androidx.room.Embedded
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ListenTrackMetadata(
    @SerialName("artist_name")
    val artist: String? = null,
    
    @SerialName("track_name")
    val track: String? = null,

    @SerialName("release_name")
    val release: String? = null,
    
    @SerialName("additional_info")
    @Embedded
    val additionalInfo: AdditionalInfo = AdditionalInfo()
) {

    fun isValid(): Boolean {
        return artist != null
                && track != null
                && additionalInfo.durationMs != null
    }
    
    override fun toString(): String {
        return "ListenTrackMetadata{" +
                "artist='" + artist + '\'' +
                ", track='" + track + '\'' +
                ", release='" + release + '\'' +
                ", durationMs=" + additionalInfo.durationMs +
                '}'
    }
}