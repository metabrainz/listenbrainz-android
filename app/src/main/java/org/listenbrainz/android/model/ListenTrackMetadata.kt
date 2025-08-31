package org.listenbrainz.android.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

class ListenTrackMetadata(
    @SerializedName("artist_name")
    var artist: String? = null,
    
    @SerializedName("track_name")
    var track: String? = null,

    @SerializedName("release_name")
    var release: String? = null,
    
    @SerializedName("additional_info")
    @Embedded
    var additionalInfo: AdditionalInfo = AdditionalInfo()
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