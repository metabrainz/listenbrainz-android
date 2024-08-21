package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName

data class ReleaseGroupData(
    val count: Int? = null,
    @SerializedName("genre_mbid") val genreMbid: String? = null,
    val tag: String? = null
)