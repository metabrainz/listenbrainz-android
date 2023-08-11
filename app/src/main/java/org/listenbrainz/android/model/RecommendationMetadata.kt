package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class RecommendationMetadata(
    
    /** The name of the track, required*/
    @SerializedName("track_name"    ) val trackName: String,
    
    /** The name of the artist, required*/
    @SerializedName("artist_name"   ) val artistName: String,
    
    /** The name of the release, optional.*/
    @SerializedName("release_name"  ) val releaseName: String,
    
    /** The MusicBrainz ID of the recording, required. */
    @SerializedName("recording_mbid") val recordingMbid: String,
    
    /** The MessyBrainz ID of the recording, optional.*/
    @SerializedName("recording_msid") val recordingMsid: String,
    
    /** Usernames of the persons user wants to recommend to. *Only required in personal recommendation.* */
    @SerializedName("users"         ) val users: List<String>? = null,

    /** String containing personalized recommendation. *Only required in personal recommendation.* */
    @SerializedName("blurb_content" ) val blurbContent: String? = null
)