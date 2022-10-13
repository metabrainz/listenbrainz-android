package org.listenbrainz.android.data.sources.api.entities

import com.google.gson.annotations.SerializedName

class WikiSummary {
    @SerializedName("displaytitle")
    var displayTitle: String? = null
    var pageId: Long = 0

    @SerializedName("originalimage")
    var originalImage: OriginalImage? = null
    var extract: String? = null

    class OriginalImage {
        var source: String? = null
        var width = 0
        var height = 0
    }
}