package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Aliase(
    @SerialName("begin-date")
    val beginDate: String? = null,
    @SerialName("end-date")
    val endDate: String? = null,
    @SerialName("locale")
    val locale: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("primary")
    val primary: JsonElement? = null,
    @SerialName("sort-name")
    val sortName: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("type-id")
    val typeId: String? = null
)