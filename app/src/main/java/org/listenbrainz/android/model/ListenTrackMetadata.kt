package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

class ListenTrackMetadata {
    @SerializedName("artist_name")
    var artist: String? = null

    @SerializedName("track_name")
    var track: String? = null

    @SerializedName("release_name")
    var release: String? = null
    
    @SerializedName("additional_info")
    var additionalInfo: AdditionalInfo = AdditionalInfo()
    
    override fun toString(): String {
        return "ListenTrackMetadata{" +
                "artist='" + artist + '\'' +
                ", track='" + track + '\'' +
                ", release='" + release + '\'' +
                '}'
    }
}