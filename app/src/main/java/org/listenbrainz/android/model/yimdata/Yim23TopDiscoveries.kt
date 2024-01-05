package org.listenbrainz.android.model.yimdata

import com.google.gson.annotations.SerializedName

data class Yim23TopDiscoveries (
    @SerializedName("playlist"    ) var playlist   : Yim23TopDiscoveriesPlaylist = Yim23TopDiscoveriesPlaylist()

)
