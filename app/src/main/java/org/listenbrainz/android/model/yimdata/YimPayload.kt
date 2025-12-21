package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.Serializable

@Serializable
data class YimPayload(
    val payload: YimPayloadData = YimPayloadData()
)