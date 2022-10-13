package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.LifeSpan
import org.listenbrainz.android.data.sources.api.entities.Link
import java.util.*

class Label : MBEntity() {
    var name: String? = null
    var type: String? = null

    @SerializedName("label-code")
    var code: String? = null
        get() = if (field != null) "LC $field" else ""

    @SerializedName("life-span")
    var lifeSpan: LifeSpan? = null
    var country: String? = null
    var area: Area? = null
    var relations: MutableList<Link> = ArrayList()
        set(relations) {
            this.relations.addAll(relations)
        }
    var releases: MutableList<Release> = ArrayList()
        set(releases) {
            this.releases.addAll(releases)
        }

    override fun toString(): String {
        return "Label{" +
                "mbid='" + mbid + '\'' +
                ", name='" + name + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                ", lifeSpan=" + lifeSpan +
                ", type='" + type + '\'' +
                ", country='" + country + '\'' +
                ", area=" + area +
                '}'
    }
}