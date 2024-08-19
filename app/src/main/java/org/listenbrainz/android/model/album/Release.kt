package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName

data class Release(
    @SerializedName("caa_id") val caaId: Long? = null,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val date: String? = null,
    val name: String? = null,
    val rels: List<Any?> = listOf(),
    val type: String? = null
)