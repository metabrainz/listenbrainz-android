package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopRecording (
    @SerialName("artist_mbids") var artistMbids: ArrayList<String> = arrayListOf(),
    @SerialName("artist_name") var artistName: String = "",
    @SerialName("listen_count") var listenCount: Int = 0,
    @SerialName("release_name") var releaseName: String = "",
    @SerialName("track_name") var trackName: String = "",
    @SerialName("recording_mbid") var recordingMbid: String? = null,
    @SerialName("release_mbid") var releaseMbid: String? = null,
    @SerialName("caa_release_mbid") var caaReleaseMbid: String? = null,
    @SerialName("caa_id") var caaId: Long? = null,
)