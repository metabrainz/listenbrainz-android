package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Data class used for both, recommendation and personal recommendation.
 * @param trackName The name of the track, required.
 * @param artistName The name of the artist, required.
 * @param releaseName The name of the release, optional.
 * @param recordingMbid The MusicBrainz ID of the recording, required.
 * @param recordingMsid The MessyBrainz ID of the recording, optional.
 * @param users Usernames of the persons user wants to recommend to. *Only required in personal recommendation.*
 * @param blurbContent String containing personalized recommendation. *Only required in personal recommendation.*
 * */
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