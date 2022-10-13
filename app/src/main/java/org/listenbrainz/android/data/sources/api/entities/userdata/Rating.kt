package org.listenbrainz.android.data.sources.api.entities.userdata

import com.google.gson.annotations.SerializedName

class Rating {
    @SerializedName("votes-count")
    var count = 0
    var value = 0f
}