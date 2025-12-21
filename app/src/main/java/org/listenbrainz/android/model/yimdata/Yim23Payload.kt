package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.Serializable

@Serializable
data class Yim23Payload(
    val payload: Yim23PayloadData = Yim23PayloadData()
)