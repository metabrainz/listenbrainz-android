package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.LifeSpan

class Event : MBEntity() {
    //TODO: Add Relations Field
    var name: String? = null

    @SerializedName("life-span")
    var lifeSpan: LifeSpan? = null
    var type: String? = null
    var time: String? = null
    override fun toString(): String {
        return "Event{" +
                "name='" + name + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                ", lifeSpan=" + lifeSpan +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                '}'
    }
}