package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.LifeSpan
import org.listenbrainz.android.data.sources.api.entities.Link
import java.util.*

class Artist : MBEntity() {
    var name: String? = null
    var country: String? = null
    var type: String? = null

    @SerializedName("sort-name")
    var sortName: String? = null
    var area: Area? = null

    @SerializedName("begin-area")
    var beginArea: Area? = null

    @SerializedName("end-area")
    var endArea: Area? = null

    @SerializedName("life-span")
    var lifeSpan: LifeSpan? = null
    var gender: String? = null
    var relations: MutableList<Link> = ArrayList()
        set(relations) {
            this.relations.addAll(relations)
        }
    @JvmField
    val releases: MutableList<Release> = ArrayList()
    fun getReleases(): List<Release> {
        return releases
    }

    fun setReleases(releases: List<Release>?) {
        this.releases.addAll(releases!!)
    }

    fun setRelease(release: Release, position: Int) {
        releases[position] = release
    }

    override fun toString(): String {
        return "Artist{" +
                "mbid='" + mbid + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                '}'
    }
}