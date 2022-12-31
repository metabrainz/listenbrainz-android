package org.listenbrainz.android.data.sources.api.entities.yimdata

import com.google.gson.annotations.SerializedName

data class TopRecording (
    
    @SerializedName("artist_mbids"     ) var artistMbids    : ArrayList<String> = arrayListOf(),
    @SerializedName("artist_name"      ) var artistName     : String           = "",
    @SerializedName("caa_id"           ) var caaId          : Long              = -1,       // -1 means we couldn't get data although
    @SerializedName("caa_release_mbid" ) var caaReleaseMbid : String           = "",        // there won't be any case whatsoever as we can check
    @SerializedName("listen_count"     ) var listenCount    : Int              = -1,        // if the data was received properly or not initially.
    @SerializedName("recording_mbid"   ) var recordingMbid  : String           = "",
    @SerializedName("release_mbid"     ) var releaseMbid    : String           = "",
    @SerializedName("release_name"     ) var releaseName    : String           = "",
    @SerializedName("track_name"       ) var trackName      : String           = ""

)