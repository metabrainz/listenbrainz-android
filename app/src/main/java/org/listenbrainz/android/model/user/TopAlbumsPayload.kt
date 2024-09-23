package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class TopAlbumsPayload(
    val count: Int? = 0,
    @SerializedName("from_ts")              val fromTs: Int? = 0,
    @SerializedName("last_updated")         val lastUpdated: Int? = 0,
    @SerializedName("offset")               val offset: Int? = 0,
                                            val range: String? = "",
                                            val releases: List<Release>? = listOf(),
    @SerializedName("to_ts")                val toTs: Int? = 0,
    @SerializedName("total_release_count")  val totalReleaseCount: Int? = 0,
    @SerializedName("user_id")              val userId: String? = ""
)