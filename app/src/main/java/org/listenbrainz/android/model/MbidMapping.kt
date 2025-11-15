package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.feed.FeedListenArtist

@Immutable
@Serializable
data class MbidMapping(
    @SerializedName("artist_mbids")
    @SerialName("artist_mbids")
    val artistMbids: List<String>,
    @SerializedName("artists")
    @SerialName("artists")
    val artists: List<FeedListenArtist>? = null,
    @SerializedName("caa_id")
    @SerialName("caa_id")
    val caaId: Long? = null,
    @SerializedName("caa_release_mbid")
    @SerialName("caa_release_mbid")
    val caaReleaseMbid: String? = null,
    @SerializedName("recording_mbid")
    @SerialName("recording_mbid")
    val recordingMbid: String? = null,
    @SerializedName("recording_name")
    @SerialName("recording_name")
    val recordingName: String? = null,
    @SerializedName("release_mbid")
    @SerialName("release_mbid")
    val releaseMbid: String? = null
)