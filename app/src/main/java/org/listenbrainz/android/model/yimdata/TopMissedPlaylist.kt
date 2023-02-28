package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class TopMissedPlaylist(
    //@SerializedName("annotation" ) var annotation    : String?          = null,
    //@SerializedName("creator"    ) var creator       : String?          = null,
    @SerializedName("identifier" ) var identifier      : String             = "",
    @SerializedName("title"      ) var title           : String             = "",
    @SerializedName("track"      ) var tracksList      : ArrayList<Track>   = arrayListOf()
)