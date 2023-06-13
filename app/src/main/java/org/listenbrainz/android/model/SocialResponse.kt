package org.listenbrainz.android.model

import org.listenbrainz.android.util.LBResponseError

data class SocialResponse(
    var status: String? = null,
    var error: LBResponseError? = null
)