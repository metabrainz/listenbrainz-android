package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName

class Collection {
    @SerializedName("id")
    var mbid: String? = null
    var type: String? = null
    var editor: String? = null
    var name: String? = null

    @SerializedName("entity-type")
    var entityType: String? = null
    var count = 0
    override fun toString(): String {
        return "Collection{" +
                "mbid='" + mbid + '\'' +
                ", type='" + type + '\'' +
                ", editor='" + editor + '\'' +
                ", name='" + name + '\'' +
                ", entityType='" + entityType + '\'' +
                ", count=" + count +
                '}'
    }
}