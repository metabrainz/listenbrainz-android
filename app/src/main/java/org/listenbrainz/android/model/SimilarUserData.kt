package org.listenbrainz.android.model

import org.listenbrainz.android.util.LBResponseError

data class SimilarUserData(
    val payload: List<SimilarUser>? = null,
    var error: LBResponseError? = null
)