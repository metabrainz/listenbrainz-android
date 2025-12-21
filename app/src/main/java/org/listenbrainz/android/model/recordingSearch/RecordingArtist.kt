package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingArtist(
    @SerialName("aliases")
    val aliases: List<Aliase?> = emptyList(),
    @SerialName("disambiguation")
    val disambiguation: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)