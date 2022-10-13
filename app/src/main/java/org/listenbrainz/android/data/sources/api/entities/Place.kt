package org.listenbrainz.android.data.sources.api.entities

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.mbentity.Area

class Place {
    @SerializedName("id")
    var mbid: String? = null
    var name: String? = null
    private var disambiguation: String? = null
    var type: String? = null
    var area: Area? = null

    @SerializedName("life-span")
    var lifeSpan: LifeSpan? = null
    var coordinates: Coordinates? = null
    override fun toString(): String {
        return "Place{" +
                "mbid='" + mbid + '\'' +
                ", name='" + name + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                ", type='" + type + '\'' +
                ", area=" + area +
                ", lifeSpan=" + lifeSpan +
                ", coordinates=" + coordinates +
                '}'
    }

    fun getdisambiguation(): String? {
        return disambiguation
    }

    fun setdisambiguation(disambiguation: String?) {
        this.disambiguation = disambiguation
    }

    class Coordinates {
        var latitude: String? = null
        var longitude: String? = null
        override fun toString(): String {
            return "Coordinates{" +
                    "latitude='" + latitude + '\'' +
                    ", longitude='" + longitude + '\'' +
                    '}'
        }
    }
}