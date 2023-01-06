package org.listenbrainz.android.data.sources.api.entities.mbentity

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.data.sources.api.entities.*
import java.util.*

class Release : MBEntity() {
    var title: String? = null

    @SerializedName("artist-credit")
    var artistCredits: MutableList<ArtistCredit> = ArrayList()
    var date: String? = null
    var barcode: String? = null
    
    @SerializedName("release-group")
    var releaseGroup: ReleaseGroup? = null

    @SerializedName("release-events")
    private val releaseEvents: MutableList<ReleaseEvent> = ArrayList()

    @SerializedName("label-info")
    private val labels: MutableList<LabelInfo> = ArrayList()

    @SerializedName("trackLis-count")
    var trackCount = 0
    var country: String? = null
    var status: String? = null
    var media: MutableList<Media>? = ArrayList()
    var coverArt: CoverArt? = null

    @SerializedName("text-representation")
    var textRepresentation: TextRepresentation? = null
    @JvmField
    val relations: MutableList<Link> = ArrayList()


    @JvmName("getTrackCount1")
    fun getTrackCount(): Int {
        if (trackCount == 0 && media != null && media!!.size != 0) {
            var count = 0
            for (medium in media!!) count += medium.trackCount
            return count
        }
        return trackCount
    }

    //TODO: Implement Text Representation
    fun labelCatalog(): String {
        val itr: Iterator<LabelInfo> = labels.iterator()
        val builder = StringBuilder()
        while (itr.hasNext()) {
            val labelInfo = itr.next()
            val catalogNumber = labelInfo.catalogNumber
            val label = labelInfo.label
            if (label != null) {
                if (catalogNumber != null && catalogNumber.isNotEmpty()) {
                    builder.append(catalogNumber).append(" (")
                    builder.append(label.name)
                    builder.append(")")
                } else builder.append(label.name)
                if (itr.hasNext()) builder.append(" , ")
            }
        }
        return builder.toString()
    }

    override fun toString(): String {
        return "Release{" +
                "title='" + title + '\'' +
                ", artistCredits=" + artistCredits +
                ", date='" + date + '\'' +
                ", barcode='" + barcode + '\'' +  //                ", packaging='" + packaging + '\'' +
                ", releaseGroup=" + releaseGroup +
                ", releaseEvents=" + releaseEvents +
                ", labels=" + labels +
                ", trackCount=" + trackCount +
                ", country='" + country + '\'' +
                ", status='" + status + '\'' +
                ", media=" + media +
                ", coverArt=" + coverArt +
                ", textRepresentation=" + textRepresentation.toString() +
                ", relations=" + relations +
                ", mbid='" + mbid + '\'' +
                ", disambiguation='" + disambiguation + '\'' +
                '}'
    }
}