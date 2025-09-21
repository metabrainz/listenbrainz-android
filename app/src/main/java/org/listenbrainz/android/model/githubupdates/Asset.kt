package org.listenbrainz.android.model.githubupdates


import com.google.gson.annotations.SerializedName

data class Asset(
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String? = null,
    @SerializedName("content_type")
    val contentType: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("digest")
    val digest: String? = null,
    @SerializedName("download_count")
    val downloadCount: Int? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("label")
    val label: Any? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("node_id")
    val nodeId: String? = null,
    @SerializedName("size")
    val size: Int? = null,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("uploader")
    val uploader: Uploader? = null,
    @SerializedName("url")
    val url: String? = null
)