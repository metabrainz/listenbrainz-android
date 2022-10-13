package org.listenbrainz.android.data.sources.api.entities.response

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.mbentity.MBEntity

open class SearchResponse<T : MBEntity?> {
    @SerializedName("created")
    var timestamp: String? = null
    var count = 0
    var offset = 0
    var items: List<T>? = null
}