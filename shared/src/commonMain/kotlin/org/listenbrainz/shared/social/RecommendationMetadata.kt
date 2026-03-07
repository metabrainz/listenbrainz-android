package org.listenbrainz.shared.social

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationMetadata(

    /** The name of the track, required*/
    @SerialName("track_name") val trackName: String = "",

    /** The name of the artist, required*/
    @SerialName("artist_name") val artistName: String = "",

    /** The name of the release, optional.*/
    @SerialName("release_name") val releaseName: String? = null,

    /** The MusicBrainz ID of the recording, required. */
    @SerialName("recording_mbid") val recordingMbid: String? = null,

    /** The MessyBrainz ID of the recording, optional.*/
    @SerialName("recording_msid") val recordingMsid: String? = null,

    /** Usernames of the persons user wants to recommend to. *Only required in personal recommendation.* */
    @SerialName("users") val users: List<String>? = null,

    /** String containing personalized recommendation. *Only required in personal recommendation.* */
    @SerialName("blurb_content") val blurbContent: String? = null
)