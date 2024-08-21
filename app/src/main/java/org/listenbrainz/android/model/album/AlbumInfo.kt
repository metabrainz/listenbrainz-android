package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName

data class AlbumInfo(
    val disambiguation: String? = null,
    @SerializedName("first-release-date") val firstReleaseDate: String? = null,
    val id: String? = null,
    @SerializedName("primary-type") val primaryType: String? = null,
    @SerializedName("primary-type-id") val primaryTypeId: String? = null,
    @SerializedName("secondary-type-ids") val secondaryTypeIds: List<Any?>? = null,
    @SerializedName("secondar-types") val secondaryTypes: List<Any?>? = null,
    val title: String? = null,
)