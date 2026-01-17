package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AlbumInfo(
    val disambiguation: String? = null,
    @SerialName("first-release-date") val firstReleaseDate: String? = null,
    val id: String? = null,
    @SerialName("primary-type") val primaryType: String? = null,
    @SerialName("primary-type-id") val primaryTypeId: String? = null,
    @SerialName("secondary-type-ids") val secondaryTypeIds: List<JsonElement?>? = null,
    @SerialName("secondar-types") val secondaryTypes: List<JsonElement?>? = null,
    val title: String? = null,
)