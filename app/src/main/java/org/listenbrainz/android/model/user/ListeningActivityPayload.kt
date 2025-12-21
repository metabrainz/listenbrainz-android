package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListeningActivityPayload(
    @SerialName("from_ts") val fromTs: Int? = null,
    @SerialName("last_updated") val lastUpdated: Int? = null,
    @SerialName("listening_activity") val listeningActivity: List<ListeningActivity?>? = null,
    val range: String? = null,
    @SerialName("to_ts") val toTs: Int? = null,
    @SerialName("user_id") val userId: String? = null,
)