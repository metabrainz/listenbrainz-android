package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialResponse(
    var status: String? = null
)