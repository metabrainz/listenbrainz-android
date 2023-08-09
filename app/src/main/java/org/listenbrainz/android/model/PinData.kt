package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class PinData (
    
    @SerializedName("recording_msid" ) var recordingMsid : String? = null,
    @SerializedName("recording_mbid" ) var recordingMbid : String? = null,
    @SerializedName("blurb_content"  ) var blurbContent  : String? = null,
    @SerializedName("pinned_until"   ) var pinnedUntil   : Int?    = null

)