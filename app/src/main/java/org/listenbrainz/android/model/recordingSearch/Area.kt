package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Area(
    @SerialName("id")
    val id: String? = null,
    @SerialName("iso-3166-1-codes")
    val iso31661Codes: List<String?> = emptyList(),
    @SerialName("name")
    val name: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)