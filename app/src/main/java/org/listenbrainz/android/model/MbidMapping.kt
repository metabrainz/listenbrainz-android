package org.listenbrainz.android.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.feed.FeedListenArtist

@Immutable
data class MbidMapping(
    @SerializedName("artist_mbids"     ) val artistMbids: List<String>,
    @SerializedName("artists"          ) val artists: List<FeedListenArtist>? = null,
    @SerializedName("caa_id"           ) val caaId: Long? = null,
    @SerializedName("caa_release_mbid" ) val caaReleaseMbid: String? = null,
    @SerializedName("recording_mbid"   ) val recordingMbid: String? = null,
    @SerializedName("recording_name"   ) val recordingName: String,
    @SerializedName("release_mbid"     ) val releaseMbid: String? = null
)