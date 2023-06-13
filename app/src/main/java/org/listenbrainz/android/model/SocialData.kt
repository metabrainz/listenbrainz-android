package org.listenbrainz.android.model

import org.listenbrainz.android.util.LBResponseError

data class SocialData (
    var followers: List<String>? = null,
    var following: List<String>? = null,
    val user: String? = null,
    var error: LBResponseError? = null
)