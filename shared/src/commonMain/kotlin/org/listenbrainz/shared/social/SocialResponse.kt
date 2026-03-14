package org.listenbrainz.shared.social

import kotlinx.serialization.Serializable

@Serializable
data class SocialResponse(
    var status: String? = null
)