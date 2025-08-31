package org.listenbrainz.android.model.githubupdates


import com.google.gson.annotations.SerializedName

data class GithubUpdatesListItem(
    @SerializedName("assets")
    val assets: List<Asset?>? = null,
    @SerializedName("assets_url")
    val assetsUrl: String? = null,
    @SerializedName("author")
    val author: Author? = null,
    @SerializedName("body")
    val body: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("draft")
    val draft: Boolean? = null,
    @SerializedName("html_url")
    val htmlUrl: String? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("immutable")
    val immutable: Boolean? = null,
    @SerializedName("mentions_count")
    val mentionsCount: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("node_id")
    val nodeId: String? = null,
    @SerializedName("prerelease")
    val prerelease: Boolean? = null,
    @SerializedName("published_at")
    val publishedAt: String? = null,
    @SerializedName("tag_name")
    val tagName: String? = null,
    @SerializedName("tarball_url")
    val tarballUrl: String? = null,
    @SerializedName("target_commitish")
    val targetCommitish: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("upload_url")
    val uploadUrl: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("zipball_url")
    val zipballUrl: String? = null
)