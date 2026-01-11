package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class Listens(
    val payload: Payload = Payload()
)