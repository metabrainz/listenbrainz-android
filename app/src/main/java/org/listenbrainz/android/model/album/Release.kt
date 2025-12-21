package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Release(
    @SerialName("caa_id") val caaId: Long? = null,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val date: String? = null,
    val name: String? = null,
    val rels: List<JsonElement?> = listOf(),
    val type: String? = null
)