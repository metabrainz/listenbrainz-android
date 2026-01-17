package org.listenbrainz.android.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserListeningActivity(
    val payload: ListeningActivityPayload? = null
)