package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.ArtistCredit
import org.listenbrainz.android.data.sources.api.entities.Link
import java.util.*

class ReleaseGroup : MBEntity() {
    var title: String? = null

    @SerializedName("primary-type")
    var primaryType: String? = null

    @SerializedName("secondary-types")
    private val secondaryTypes: MutableList<String> = ArrayList()
    var count = 0

    //TODO: Implement correct wrapper JSON
    @SerializedName("artist-credit")
    val artistCredits: MutableList<ArtistCredit> = ArrayList()
    @JvmField
    val relations: MutableList<Link> = ArrayList()
    @JvmField
    val releases: MutableList<Release> = ArrayList()
    fun getReleases(): List<Release> {
        return releases
    }

    fun setReleases(releases: List<Release>?) {
        this.releases.addAll(releases!!)
    }

    fun getRelations(): List<Link> {
        return relations
    }

    fun setRelations(relations: List<Link>?) {
        this.relations.addAll(relations!!)
    }

    @JvmName("getArtistCredits1")
    fun getArtistCredits(): MutableList<ArtistCredit> {
        return artistCredits
    }

    fun setArtistCredits(artistCredits: List<ArtistCredit>?) {
        this.artistCredits.addAll(artistCredits!!)
    }

    fun getSecondaryTypes(): List<String> {
        return secondaryTypes
    }

    fun setSecondaryTypes(secondaryTypes: List<String>?) {
        this.secondaryTypes.addAll(secondaryTypes!!)
    }

    val fullType: String?
        get() = if (secondaryTypes.size != 0) {
            var type = primaryType
            for (string in secondaryTypes) type = "$type + $string"
            type
        } else primaryType

    override fun toString(): String {
        return "ReleaseGroup{" +
                "title='" + title + '\'' +
                ", primaryType='" + primaryType + '\'' +
                ", secondaryTypes=" + secondaryTypes +
                ", count=" + count +
                ", artistCredits=" + artistCredits +
                ", relations=" + relations +
                ", releases=" + releases +
                ", mbid='" + mbid + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                '}'
    }
}