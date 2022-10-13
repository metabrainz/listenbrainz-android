package org.listenbrainz.android.data.sources.api.entities.userdata

import com.google.gson.annotations.SerializedName

internal class Collection {
    @SerializedName("id")
    var mbid: String? = null
    var type: String? = null
    var editor: String? = null
    var name: String? = null

    @SerializedName("entity-type")
    var entityType: String? = null
    var count = 0
}