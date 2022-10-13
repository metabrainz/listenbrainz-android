package org.listenbrainz.android.data.sources.api.entities.response

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.mbentity.Collection
import java.util.*

class CollectionListResponse {
    @SerializedName("collection-offset")
    var offset: String? = null

    @SerializedName("collection-limit")
    var limit: String? = null
    var collections: List<Collection> = ArrayList()
}