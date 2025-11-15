package org.listenbrainz.android.model

import kotlinx.serialization.Serializable

@Serializable
data class SocialResponse(
    var status: String? = null
)