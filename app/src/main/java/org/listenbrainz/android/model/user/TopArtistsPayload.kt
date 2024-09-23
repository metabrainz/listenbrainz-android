package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class TopArtistsPayload(
                                          val artists: List<Artist>,
                                          val count: Int,
    @SerializedName("from_ts")            val fromTs: Int,
    @SerializedName("last_updated")       val lastUpdated: Int,
                                          val offset: Int,
                                          val range: String,
    @SerializedName("to_ts")              val toTs: Int,
    @SerializedName("total_artist_count") val totalArtistCount: Int,
    @SerializedName("user_id")            val userId: String
)