package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class TopRecording (
    
    @SerializedName("artist_mbids"     ) var artistMbids    : ArrayList<String> = arrayListOf(),
    @SerializedName("artist_name"      ) var artistName     : String           = "",
    @SerializedName("listen_count"     ) var listenCount    : Int              = 0,
    @SerializedName("release_name"     ) var releaseName    : String           = "",
    @SerializedName("track_name"       ) var trackName      : String           = "",
    @SerializedName("recording_mbid"   ) var recordingMbid  : String?          = null,
    @SerializedName("release_mbid"     ) var releaseMbid    : String?          = null,
    @SerializedName("caa_release_mbid"   ) var caaReleaseMbid : String?          = null,
    @SerializedName("caa_id"           ) var caaId          : Long?            = null,

)