package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Review(
    val created: String? = null,
    val edits: Int? = null,
    @SerialName("entity_id") val entityId: String? = null,
    @SerialName("entity_type") val entityType: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val id: String? = null,
    @SerialName("info_url") val infoUrl: String? = null,
    @SerialName("is_draft") val isDraft: Boolean? = null,
    @SerialName("is_hidden") val isHidden: Boolean? = null,
    val language: String? = null,
    @SerialName("last_revision") val lastRevision: LastRevision? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("license_id") val licenseId: String? = null,
    val popularity: Int? = null,
    @SerialName("published_on") val publishedOn: String? = null,
    val rating: Int? = null,
    val source: JsonElement? = null,
    @SerialName("source_url") val sourceUrl: JsonElement? = null,
    val text: String? = null,
    val user: User? = null,
    @SerialName("votes_negative_count") val votesNegativeCount: Int? = null,
    @SerialName("votes_positive_count") val votesPositiveCount: Int? = null
)