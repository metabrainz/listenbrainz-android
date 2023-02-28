package org.listenbrainz.android.model

data class Payload(
    val count: Int,
    val latest_listen_ts: Int,
    val listens: List<Listen>,
    val user_id: String
)