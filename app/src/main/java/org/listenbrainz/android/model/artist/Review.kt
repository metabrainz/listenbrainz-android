package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class Review(
    val created: String? = null,
    val edits: Int? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("entity_type") val entityType: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val id: String? = null,
    @SerializedName("info_url") val infoUrl: String? = null,
    @SerializedName("is_draft") val isDraft: Boolean? = null,
    @SerializedName("is_hidden") val isHidden: Boolean? = null,
    val language: String? = null,
    @SerializedName("last_revision") val lastRevision: LastRevision? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null,
    @SerializedName("license_id") val licenseId: String? = null,
    val popularity: Int? = null,
    @SerializedName("published_on") val publishedOn: String? = null,
    val rating: Int? = null,
    val source: Any? = null,
    @SerializedName("source_url") val sourceUrl: Any? = null,
    val text: String? = null,
    val user: User? = null,
    @SerializedName("votes_negative_count") val votesNegativeCount: Int? = null,
    @SerializedName("votes_positive_count") val votesPositiveCount: Int? = null
)