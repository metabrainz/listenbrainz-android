package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingReleaseGroup(
    @SerialName("id")
    val id: String? = null,
    @SerialName("primary-type")
    val primaryType: String? = null,
    @SerialName("primary-type-id")
    val primaryTypeId: String? = null,
    @SerialName("secondary-type-ids")
    val secondaryTypeIds: List<String?> = emptyList(),
    @SerialName("secondary-types")
    val secondaryTypes: List<String?> = emptyList(),
    @SerialName("title")
    val title: String? = null,
    @SerialName("type-id")
    val typeId: String? = null
)