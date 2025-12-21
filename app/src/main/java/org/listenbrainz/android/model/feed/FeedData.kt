package org.listenbrainz.android.model.feed

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class FeedData(
    val payload: FeedPayload = FeedPayload()
)