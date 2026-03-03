package org.listenbrainz.android.model.artistSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistData(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("score")
    val score: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("sort-name")
    val sortName: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("area")
    val area: ArtistArea? = null,
    @SerialName("begin-area")
    val beginArea: ArtistArea? = null,
    @SerialName("end-area")
    val endArea: ArtistArea? = null,
    @SerialName("disambiguation")
    val disambiguation: String? = null,
    @SerialName("life-span")
    val lifeSpan: ArtistLifeSpan? = null,
    @SerialName("aliases")
    val aliases: List<ArtistAliase> = emptyList(),
    @SerialName("tags")
    val tags: List<ArtistTag> = emptyList(),
)
