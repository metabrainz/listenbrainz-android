package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class ListeningActivityPayload(
    @SerializedName("from_ts")            val fromTs: Int? = null,
    @SerializedName("last_updated")       val lastUpdated: Int? = null,
    @SerializedName("listening_activity") val listeningActivity: List<ListeningActivity?>? = null,
                                          val range: String? = null,
    @SerializedName("to_ts")              val toTs: Int? = null,
    @SerializedName("user_id")            val userId: String? = null,
)