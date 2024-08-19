package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.feed.FeedListenArtist

data class Track(
    @SerializedName("artist_mbids") val artistMbids: List<String?>? = null,
    val artists: List<FeedListenArtist?>? = null,
    val length: Int? = null,
    val name: String? = null,
    val position: Int? = null,
    @SerializedName("recording_mbid") val recordingMbid: String? = null,
    @SerializedName("total_listen_count") val totalListenCount: Int? = null,
    @SerializedName("total_user_count") val totalUserCount: Int? = null
)