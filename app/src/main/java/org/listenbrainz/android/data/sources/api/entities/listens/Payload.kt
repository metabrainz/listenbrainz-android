package org.listenbrainz.android.data.sources.api.entities.listens

data class Payload(
    val count: Int,
    val latest_listen_ts: Int,
    val listens: List<Listen>,
    val user_id: String
)